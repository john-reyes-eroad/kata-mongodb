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

    public List<Trip> findAll() {
        List<Document> documents = collection.find().into(new ArrayList<>());
        return documents.stream().map(this::toTrip).toList();
    }

    public Optional<Trip> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        if (document == null) {
            return Optional.empty();
        }
        return Optional.of(toTrip(document));
    }

    public Map<String, Trip> findByIds(Collection<String> ids) {
        LinkedHashSet<ObjectId> objectIds = new LinkedHashSet<>();
        for (String id : ids) {
            ObjectId objectId = parseObjectId(id);
            if (objectId != null) {
                objectIds.add(objectId);
            }
        }
        if (objectIds.isEmpty()) {
            return Map.of();
        }

        List<Document> documents = collection.find(Filters.in("_id", objectIds)).into(new ArrayList<>());
        List<Trip> trips = documents.stream().map(this::toTrip).toList();
        LinkedHashMap<String, Trip> tripsById = new LinkedHashMap<>();
        for (Trip trip : trips) {
            if (trip.id() != null) {
                tripsById.put(trip.id(), trip);
            }
        }
        return tripsById;
    }

    public long count(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return collection.countDocuments();
        }

        ObjectId objectId = parseObjectId(keyword);
        if (objectId == null) {
            return 0;
        }
        return collection.countDocuments(Filters.or(
                eq("_id", objectId),
                eq("vehicleId", objectId),
                eq("driverId", objectId)));
    }

    public Trip save(Trip trip) {
        ObjectId objectId = parseObjectId(trip.id());
        if (objectId == null) {
            objectId = new ObjectId();
            Trip created = new Trip(
                    objectId.toHexString(),
                    trip.vehicle(),
                    trip.driver(),
                    trip.startTime(),
                    trip.endTime(),
                    trip.distanceKm(),
                    trip.createdAt(),
                    trip.updatedAt()
            );
            collection.insertOne(toDocument(created, objectId));
            return created;
        }

        if (collection.replaceOne(eq("_id", objectId), toDocument(trip, objectId),
                new ReplaceOptions().upsert(false)).getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Trip not found: " + trip.id());
        }
        return trip;
    }

    public void delete(Trip trip) {
        ObjectId objectId = parseObjectId(trip.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
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
        ObjectId id = document.getObjectId("_id");
        ObjectId vehicleId = document.getObjectId("vehicleId");
        ObjectId driverId = document.getObjectId("driverId");
        return new Trip(
                id == null ? null : id.toHexString(),
                vehicleId == null ? null : new Vehicle(vehicleId.toHexString(), null, null, null, 0, null, null),
                driverId == null ? null : new Driver(driverId.toHexString(), null, null, null, null),
                toInstant(document.getDate("startTime")),
                toInstant(document.getDate("endTime")),
                toBigDecimal(document.get("distanceKm")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
