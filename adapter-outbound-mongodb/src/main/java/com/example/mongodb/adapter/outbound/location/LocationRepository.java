package com.example.mongodb.adapter.outbound.location;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.location.Location;
import com.example.mongocrud.location.port.outbound.LocationPersistencePort;
import com.example.mongocrud.trip.Trip;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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
public class LocationRepository implements LocationPersistencePort {

    private final MongoCollection<Document> collection;

    public LocationRepository(MongoDatabase database) {
        this.collection = database.getCollection("locations");
    }

    @Override
    public List<Location> findAll() {
        return collection.find().map(this::toLocation).into(new ArrayList<>());
    }

    @Override
    public Optional<Location> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        if (document == null) {
            return Optional.empty();
        }
        return Optional.of(toLocation(document));
    }

    @Override
    public long count(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return collection.countDocuments();
        }

        ObjectId objectId = parseObjectId(keyword);
        if (objectId == null) {
            return 0;
        }
        return collection.countDocuments(com.mongodb.client.model.Filters.or(
                eq("_id", objectId),
                eq("tripId", objectId)));
    }

    @Override
    public Location save(Location location) {
        ObjectId objectId = parseObjectId(location.id());
        if (objectId == null) {
            objectId = new ObjectId();
            Location created = new Location(
                    objectId.toHexString(),
                    location.trip(),
                    location.latitude(),
                    location.longitude(),
                    location.recordedAt(),
                    location.createdAt(),
                    location.updatedAt()
            );
            collection.insertOne(toDocument(created, objectId));
            return created;
        }

        if (collection.replaceOne(eq("_id", objectId), toDocument(location, objectId),
                new ReplaceOptions().upsert(false)).getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Location not found: " + location.id());
        }
        return location;
    }

    @Override
    public void delete(Location location) {
        ObjectId objectId = parseObjectId(location.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(Location location, ObjectId id) {
        return new Document("_id", id)
                .append("tripId", parseObjectId(location.trip() == null ? null : location.trip().id()))
                .append("latitude", toDecimal128(location.latitude()))
                .append("longitude", toDecimal128(location.longitude()))
                .append("recordedAt", toDate(location.recordedAt()))
                .append("createdAt", toDate(location.createdAt()))
                .append("updatedAt", toDate(location.updatedAt()));
    }

    private Location toLocation(Document document) {
        ObjectId id = document.getObjectId("_id");
        ObjectId tripId = document.getObjectId("tripId");
        return new Location(
                id == null ? null : id.toHexString(),
                tripId == null ? null : new Trip(tripId.toHexString()),
                toBigDecimal(document.get("latitude")),
                toBigDecimal(document.get("longitude")),
                toInstant(document.getDate("recordedAt")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
