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

## Step 11: Aggregation framework (Mongo equivalent of complex SQL)

Think of aggregation pipelines like composable SQL stages.

```javascript
db.trips.aggregate([
  { $match: { distanceKm: { $gte: 100 } } },
  { $group: { _id: "$driverId", total: { $sum: "$distanceKm" } } },
  { $sort: { total: -1 } }
])
```

## Step 12: Java usage in this repo (driver-level)

This codebase uses:
- `mongodb-driver-sync`
- Explicit repositories (`MongoCollection<Document>`)
- Manual mapping between `Document` and domain records

Important patterns:
1. Parse string IDs to `ObjectId`.
2. Use repository-level batch fetch (`findByIds`) to avoid N+1 lookups.
3. Create indexes at startup.
4. Convert temporal and numeric types explicitly (Instant, Decimal128).

## Step 13: Practice checklist (do these in order)

1. Create a collection and insert 10 sample documents in `mongosh`.
2. Add a unique index and test duplicate key failure.
3. Run a regex search and inspect explain plan.
4. Build one aggregation pipeline with `match -> group -> sort`.
5. Model one entity pair both ways: embedded vs referenced.
6. Benchmark list endpoint behavior with and without N+1.

## Step 14: Common mistakes to avoid

- Treating MongoDB like PostgreSQL with too many normalized collections.
- Forgetting indexes on query fields.
- Doing per-document lookups in loops (N+1 problem).
- Storing inconsistent date/number types.
- Using `$regex` on unindexed large fields in hot paths.

## Step 15: 7-day learning plan

1. Day 1: CRUD + shell basics + ObjectId
2. Day 2: Indexes + explain plans
3. Day 3: Data modeling (embed vs reference)
4. Day 4: Aggregation fundamentals
5. Day 5: Transactions + consistency strategy
6. Day 6: Driver-level coding patterns in Java
7. Day 7: Mini-project: implement one domain end-to-end

---

If you master Steps 1, 6, 7, and 11 first, you’ll avoid most production mistakes when transitioning from PostgreSQL to MongoDB.
