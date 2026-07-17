package com.example.mongodb.adapter.outbound.trip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.trip.Trip;
import com.example.mongocrud.trip.port.outbound.TripPersistencePort;
import com.example.mongocrud.vehicle.Vehicle;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import static com.example.mongodb.adapter.outbound.common.MongoSupport.parseObjectId;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toBigDecimal;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toDate;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toDecimal128;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toInstant;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class TripRepository implements TripPersistencePort {

    private static final List<Document> LOOKUP_PIPELINE = List.of(
        new Document("$lookup", new Document()
            .append("from", "vehicles")
            .append("localField", "vehicleId")
            .append("foreignField", "_id")
            .append("as", "vehicle")),
        new Document("$unwind", new Document()
            .append("path", "$vehicle")
            .append("preserveNullAndEmptyArrays", true)),
        new Document("$lookup", new Document()
            .append("from", "drivers")
            .append("localField", "driverId")
            .append("foreignField", "_id")
            .append("as", "driver")),
        new Document("$unwind", new Document()
            .append("path", "$driver")
            .append("preserveNullAndEmptyArrays", true))
    );

    private final MongoCollection<Document> collection;

    public TripRepository(MongoDatabase database) {
        this.collection = database.getCollection("trips");
    }

    @Override
    public List<Trip> findAll() {
        var pipeline = new ArrayList<>(LOOKUP_PIPELINE);
        return collection
            .aggregate(pipeline)
            .map(this::toTrip)
            .into(new ArrayList<>());
    }

    @Override
    public Optional<Trip> findById(String id) {
        var objectId = parseObjectId(id);

        if (objectId == null) {
            return Optional.empty();
        }

        var pipeline = new ArrayList<Document>();
        pipeline.add(new Document("$match", eq("_id", objectId)));
        pipeline.addAll(LOOKUP_PIPELINE);

        var document = collection
            .aggregate(pipeline)
            .first();

        return document == null
            ? Optional.empty()
            : Optional.of(toTrip(document));
    }

    @Override
    public Map<String, Trip> findByIds(Collection<String> ids) {
        var objectIds = new LinkedHashSet<ObjectId>();

        for (var id : ids) {
            var objectId = parseObjectId(id);
            if (objectId != null) {
                objectIds.add(objectId);
            }
        }

        if (objectIds.isEmpty()) {
            return Map.of();
        }

        var pipeline = new ArrayList<Document>();
        pipeline.add(new Document("$match", Filters.in("_id", objectIds)));
        pipeline.addAll(LOOKUP_PIPELINE);

        var tripsById = new LinkedHashMap<String, Trip>();
        for (var document : collection.aggregate(pipeline).into(new ArrayList<>())) {
            var trip = toTrip(document);
            if (trip.id() != null) {
                tripsById.put(trip.id(), trip);
            }
        }

        return tripsById;
    }

    @Override
    public long count(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return collection.countDocuments();
        }

        var objectId = parseObjectId(keyword);
        if (objectId == null) {
            return 0;
        }

        return collection.countDocuments(Filters.or(
            eq("_id", objectId),
            eq("vehicleId", objectId),
            eq("driverId", objectId)
        ));
    }

    @Override
    public Trip save(Trip trip) {
        var objectId = parseObjectId(trip.id());

        if (objectId == null) {
            objectId = new ObjectId();
            var created = new Trip(
                objectId.toHexString(),
                trip.vehicle(),
                trip.driver(),
                trip.startTime(),
                trip.endTime(),
                trip.distanceKm(),
                trip.createdAt(),
                trip.updatedAt()
            );
            collection
                .insertOne(toDocument(created, objectId));
            return created;
        }

        var updateResult = collection
            .replaceOne(
                eq("_id", objectId),
                toDocument(trip, objectId),
                new ReplaceOptions().upsert(false)
            );

        if (updateResult.getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Trip not found: " + trip.id());
        }

        return trip;
    }

    @Override
    public void delete(Trip trip) {
        var objectId = parseObjectId(trip.id());
        if (objectId != null) {
            collection
                .deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(Trip trip, ObjectId id) {
        return new Document("_id", id)
            .append("vehicleId", parseObjectId(trip.vehicle() == null ? null : trip.vehicle().id()))
            .append("driverId", parseObjectId(trip.driver() == null ? null : trip.driver().id()))
            .append("startTime", toDate(trip.startTime()))
            .append("endTime", toDate(trip.endTime()))
            .append("distanceKm", toDecimal128(trip.distanceKm()))
            .append("createdAt", toDate(trip.createdAt()))
            .append("updatedAt", toDate(trip.updatedAt()));
    }

    private Trip toTrip(Document document) {
        var id = document.getObjectId("_id");
        return new Trip(
            id == null ? null : id.toHexString(),
            toVehicle(document.get("vehicle", Document.class)),
            toDriver(document.get("driver", Document.class)),
            toInstant(document.getDate("startTime")),
            toInstant(document.getDate("endTime")),
            toBigDecimal(document.get("distanceKm")),
            toInstant(document.getDate("createdAt")),
            toInstant(document.getDate("updatedAt"))
        );
    }

    private Vehicle toVehicle(Document doc) {
        if (doc == null) {
            return null;
        }
        var id = doc.getObjectId("_id");
        var year = doc.get("year", Number.class);
        return new Vehicle(
            id == null ? null : id.toHexString(),
            doc.getString("vin"),
            doc.getString("make"),
            doc.getString("model"),
            year == null ? 0 : year.intValue(),
            toInstant(doc.getDate("createdAt")),
            toInstant(doc.getDate("updatedAt"))
        );
    }

    private Driver toDriver(Document doc) {
        if (doc == null) {
            return null;
        }
        var id = doc.getObjectId("_id");
        return new Driver(
            id == null ? null : id.toHexString(),
            doc.getString("name"),
            doc.getString("licenseNumber"),
            toInstant(doc.getDate("createdAt")),
            toInstant(doc.getDate("updatedAt"))
        );
    }
}
