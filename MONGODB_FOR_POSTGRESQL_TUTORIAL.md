# MongoDB for PostgreSQL Developers: Step-by-Step Tutorial

This guide is focused on the **critical MongoDB knowledge** you need if you already know PostgreSQL.

## Step 1: Shift your mental model

PostgreSQL is row/table-first. MongoDB is document-first.

- PostgreSQL: table -> rows -> joins
- MongoDB: collection -> BSON documents -> embed or reference

Think in terms of:
1. **One request = one document read when possible**
2. **Denormalize for read patterns**
3. **Use references only when data changes independently**

## Step 2: Learn the core object mapping

| PostgreSQL | MongoDB |
|---|---|
| Database | Database |
| Schema + Table | Collection |
| Row | Document |
| Primary key (`id`) | `_id` (usually ObjectId) |
| Index | Index |
| FK + JOIN | Reference + app-side join or `$lookup` |

## Step 3: Start with `mongosh` basics

```bash
mongosh "mongodb://localhost:27017/kata_mongodb"
```

Common commands:

```javascript
show dbs
use kata_mongodb
show collections
db.vehicles.find().limit(5)
db.vehicles.findOne({ vin: "VIN-123" })
```

## Step 4: CRUD equivalents (SQL -> MongoDB)

### Insert
```sql
INSERT INTO vehicles(vin, make) VALUES ('VIN-1', 'FleetCo');
```
```javascript
db.vehicles.insertOne({ vin: "VIN-1", make: "FleetCo" })
```

### Select
```sql
SELECT * FROM vehicles WHERE make = 'FleetCo';
```
```javascript
db.vehicles.find({ make: "FleetCo" })
```

### Update
```sql
UPDATE vehicles SET make = 'COVESA' WHERE vin = 'VIN-1';
```
```javascript
db.vehicles.updateOne(
  { vin: "VIN-1" },
  { $set: { make: "COVESA" } }
)
```

### Delete
```sql
DELETE FROM vehicles WHERE vin = 'VIN-1';
```
```javascript
db.vehicles.deleteOne({ vin: "VIN-1" })
```

## Step 5: Understand MongoDB query operators

You’ll use these often:

- `$in`, `$nin`
- `$gt`, `$gte`, `$lt`, `$lte`
- `$and`, `$or`
- `$regex`
- `$exists`

Example:
```javascript
db.vehicles.find({
  $or: [
    { vin: { $regex: "abc", $options: "i" } },
    { make: { $regex: "abc", $options: "i" } }
  ]
})
```

## Step 6: Indexes are still mandatory

MongoDB also needs indexing discipline.

Create unique indexes:
```javascript
db.vehicles.createIndex({ vin: 1 }, { unique: true })
db.drivers.createIndex({ licenseNumber: 1 }, { unique: true })
```

Inspect:
```javascript
db.vehicles.getIndexes()
db.vehicles.find({ vin: "VIN-1" }).explain("executionStats")
```

## Step 7: Model relationships correctly (critical)

Use this rule:

1. **Embed** if child lifecycle is tied to parent and read together always.
2. **Reference** if entities are reused or updated independently.

### PostgreSQL comparison

In PostgreSQL, you usually normalize first:

- `trips(vehicle_id, driver_id)` with foreign keys
- join when reading combined data

In MongoDB, you decide between:

- embed child data directly inside the parent document
- store child in its own collection and keep only IDs (reference)

### Embedding example

If location points are only meaningful inside one trip and always read with that trip:

```javascript
{
  _id: ObjectId("..."),
  vehicleId: ObjectId("..."),
  driverId: ObjectId("..."),
  startTime: ISODate("2026-07-15T00:00:00Z"),
  locations: [
    { latitude: -36.8485, longitude: 174.7633, recordedAt: ISODate("...") },
    { latitude: -36.8500, longitude: 174.7650, recordedAt: ISODate("...") }
  ]
}
```

PostgreSQL equivalent:

- `trips` table
- `locations` table with `trip_id` FK
- join on read

Embedding wins when reads are trip-centric and location churn is scoped to that trip.

### Referencing example

If entities are reused independently (as in this project), use references:

```javascript
// trips
{
  _id: ObjectId("trip1"),
  vehicleId: ObjectId("veh1"),
  driverId: ObjectId("drv1"),
  distanceKm: NumberDecimal("120.7")
}

// diagnostic_events
{
  _id: ObjectId("diag1"),
  vehicleId: ObjectId("veh1"),
  code: "P0001"
}
```

PostgreSQL equivalent:

- standard normalized tables with FKs
- joins for relationship traversal

Referencing wins when parent and child are updated separately or shared by many documents.

### Quick decision table

| Question | Prefer embedding | Prefer referencing |
|---|---|---|
| Child reused by multiple parents? | No | Yes |
| Need independent updates/deletes? | No | Yes |
| Read parent+child together most of the time? | Yes | Sometimes/No |
| Child array can grow very large/unbounded? | No | Yes |

In this repo:

- `trip` references `vehicle` and `driver`
- `location` references `trip`
- `diagnosticEvent` references `vehicle`

## Step 8: Learn update semantics

Mongo updates are operator-based, not full-row by default.

- `$set` updates fields
- `$inc` increments numbers
- `$push` appends to arrays

Without operators, replacements overwrite the whole document.

## Step 9: Handle transactions carefully

MongoDB supports multi-document transactions, but use them only when required.

- Single-document writes are atomic by default.
- Design to keep operations in one document where possible.
- Use transactions for true multi-document consistency needs.

## Step 10: Know consistency and constraints differences

What you had in PostgreSQL and MongoDB equivalents:

- FK constraints: app-level enforcement or validation logic
- JOINs: app-side composition or aggregation `$lookup`
- CHECK constraints: schema validation / app validation
- SERIAL/BIGSERIAL: ObjectId/UUID

## Step 11: MongoDB join operations (`$lookup`) vs PostgreSQL joins

PostgreSQL joins are first-class in normal query flow:

```sql
SELECT t.id, v.vin, d.name
FROM trips t
JOIN vehicles v ON v.id = t.vehicle_id
JOIN drivers d ON d.id = t.driver_id
WHERE t.distance_km >= 100;
```

MongoDB equivalent uses aggregation with `$lookup`:

```javascript
db.trips.aggregate([
  { $match: { distanceKm: { $gte: NumberDecimal("100") } } },
  {
    $lookup: {
      from: "vehicles",
      localField: "vehicleId",
      foreignField: "_id",
      as: "vehicle"
    }
  },
  {
    $lookup: {
      from: "drivers",
      localField: "driverId",
      foreignField: "_id",
      as: "driver"
    }
  },
  { $unwind: "$vehicle" },
  { $unwind: "$driver" },
  {
    $project: {
      _id: 1,
      distanceKm: 1,
      "vehicle.vin": 1,
      "driver.name": 1
    }
  }
])
```

How to think about it:

- PostgreSQL: normalize first, join often.
- MongoDB: model for read patterns first, join only when needed.
- If you need `$lookup` in every hot query, reconsider embedding or read model denormalization.

## Step 12: Aggregation framework (Mongo equivalent of complex SQL)

Think of aggregation pipelines like composable SQL stages.

```javascript
db.trips.aggregate([
  { $match: { distanceKm: { $gte: 100 } } },
  { $group: { _id: "$driverId", total: { $sum: "$distanceKm" } } },
  { $sort: { total: -1 } }
])
```

## Step 13: Java usage in this repo (driver-level)

This codebase uses:
- `mongodb-driver-sync`
- Explicit repositories (`MongoCollection<Document>`)
- Manual mapping between `Document` and domain records

Important patterns:
1. Parse string IDs to `ObjectId`.
2. Use aggregation pipelines with `$lookup` to enrich reads in a single round-trip.
3. Create indexes at startup.
4. Convert temporal and numeric types explicitly (Instant, Decimal128).

### Aggregation pipeline with `$lookup` in Java

When a document references other collections (e.g. `trips` references `vehicles` and `drivers`), use an aggregation pipeline to fetch and embed the related documents server-side — one query, no N+1.

Define the shared lookup stages once as a constant:

```java
private static final List<Document> LOOKUP_PIPELINE = List.of(
    new Document("$lookup", new Document()
        .append("from", "vehicles")
        .append("localField", "vehicleId")
        .append("foreignField", "_id")
        .append("as", "vehicle")),
    new Document("$unwind", new Document()
        .append("path", "$vehicle")
        .append("preserveNullAndEmptyArrays", true)),  // keep trips with no vehicle
    new Document("$lookup", new Document()
        .append("from", "drivers")
        .append("localField", "driverId")
        .append("foreignField", "_id")
        .append("as", "driver")),
    new Document("$unwind", new Document()
        .append("path", "$driver")
        .append("preserveNullAndEmptyArrays", true))
);
```

For `findAll`, pass the pipeline directly to `aggregate()`:

```java
public List<Trip> findAll() {
    var pipeline = new ArrayList<>(LOOKUP_PIPELINE);
    return collection
        .aggregate(pipeline)
        .map(this::toTrip)
        .into(new ArrayList<>());
}
```

For `findById` and `findByIds`, prepend a `$match` stage then append the shared pipeline:

```java
public Optional<Trip> findById(String id) {
    var objectId = parseObjectId(id);
    if (objectId == null) return Optional.empty();

    var pipeline = new ArrayList<Document>();
    pipeline.add(new Document("$match", eq("_id", objectId)));
    pipeline.addAll(LOOKUP_PIPELINE);

    var document = collection.aggregate(pipeline).first();
    return document == null ? Optional.empty() : Optional.of(toTrip(document));
}
```

After `$lookup` + `$unwind`, the enriched document contains the full embedded subdoc. Map it in the domain mapper:

```java
private Trip toTrip(Document document) {
    var id = document.getObjectId("_id");
    return new Trip(
        id == null ? null : id.toHexString(),
        toVehicle(document.get("vehicle", Document.class)),  // full Vehicle, not just id stub
        toDriver(document.get("driver", Document.class)),
        toInstant(document.getDate("startTime")),
        ...
    );
}
```

### PostgreSQL mental model mapping

| PostgreSQL | Java + MongoDB driver |
|---|---|
| `JOIN vehicles ON vehicle_id = vehicles.id` | `$lookup` + `$unwind` in aggregation pipeline |
| Single `SELECT` with `JOIN` | Single `collection.aggregate(pipeline)` call |
| `LEFT JOIN` (null-safe) | `$unwind` with `preserveNullAndEmptyArrays: true` |
| `WHERE id = ?` before join | `$match` stage prepended before lookup stages |

## Step 14: Practice checklist (do these in order)

1. Create a collection and insert 10 sample documents in `mongosh`.
2. Add a unique index and test duplicate key failure.
3. Run a regex search and inspect explain plan.
4. Build one aggregation pipeline with `match -> group -> sort`.
5. Model one entity pair both ways: embedded vs referenced.
6. Benchmark list endpoint behavior with and without N+1.

## Step 15: Common mistakes to avoid

- Treating MongoDB like PostgreSQL with too many normalized collections.
- Forgetting indexes on query fields.
- Doing per-document lookups in loops (N+1 problem).
- Storing inconsistent date/number types.
- Using `$regex` on unindexed large fields in hot paths.

## Step 16: 7-day learning plan

1. Day 1: CRUD + shell basics + ObjectId
2. Day 2: Indexes + explain plans
3. Day 3: Data modeling (embed vs reference)
4. Day 4: Aggregation fundamentals
5. Day 5: Transactions + consistency strategy
6. Day 6: Driver-level coding patterns in Java
7. Day 7: Mini-project: implement one domain end-to-end

---

If you master Steps 1, 6, 7, 11, and 12 first, you’ll avoid most production mistakes when transitioning from PostgreSQL to MongoDB.
