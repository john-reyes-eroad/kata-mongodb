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

    private final MongoCollection<Document> collection;

    public TripRepository(MongoDatabase database) {
        this.collection = database.getCollection("trips");
    }

    @Override
    public List<Trip> findAll() {
        return collection.find().map(this::toTrip).into(new ArrayList<>());
    }

    @Override
    public Optional<Trip> findById(String id) {
        var objectId = parseObjectId(id);

        if (objectId == null) {
            return Optional.empty();
        }

        var document = collection
            .find(eq("_id", objectId))
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

        var documents = collection
            .find(Filters.in("_id", objectIds))
            .into(new ArrayList<>());

        var tripsById = new LinkedHashMap<String, Trip>();
        for (var document : documents) {
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
        var vehicleId = document.getObjectId("vehicleId");
        var driverId = document.getObjectId("driverId");
        return new Trip(
            id == null ? null : id.toHexString(),
            vehicleId == null ? null : new Vehicle(vehicleId.toHexString()),
            driverId == null ? null : new Driver(driverId.toHexString()),
            toInstant(document.getDate("startTime")),
            toInstant(document.getDate("endTime")),
            toBigDecimal(document.get("distanceKm")),
            toInstant(document.getDate("createdAt")),
            toInstant(document.getDate("updatedAt"))
        );
    }
}
