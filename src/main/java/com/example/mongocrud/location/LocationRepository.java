package com.example.mongocrud.location;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.trip.TripRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import static com.example.mongocrud.common.MongoSupport.parseObjectId;
import static com.example.mongocrud.common.MongoSupport.toBigDecimal;
import static com.example.mongocrud.common.MongoSupport.toDate;
import static com.example.mongocrud.common.MongoSupport.toDecimal128;
import static com.example.mongocrud.common.MongoSupport.toInstant;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class LocationRepository {

    private final MongoCollection<Document> collection;
    private final TripRepository tripRepository;

    public LocationRepository(MongoDatabase database, TripRepository tripRepository) {
        this.collection = database.getCollection("locations");
        this.tripRepository = tripRepository;
    }

    public List<Location> findAll() {
        List<Document> documents = collection.find().into(new ArrayList<>());
        LinkedHashSet<String> tripIds = new LinkedHashSet<>();
        for (Document document : documents) {
            ObjectId tripId = document.getObjectId("tripId");
            if (tripId != null) {
                tripIds.add(tripId.toHexString());
            }
        }
        Map<String, com.example.mongocrud.trip.Trip> tripsById = tripRepository.findByIds(tripIds);

        List<Location> locations = new ArrayList<>(documents.size());
        for (Document document : documents) {
            locations.add(toLocation(document, tripsById));
        }
        return locations;
    }

    public Optional<Location> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        if (document == null) {
            return Optional.empty();
        }
        ObjectId tripId = document.getObjectId("tripId");
        Map<String, com.example.mongocrud.trip.Trip> tripsById = tripId == null
                ? Map.of()
                : tripRepository.findByIds(List.of(tripId.toHexString()));
        return Optional.of(toLocation(document, tripsById));
    }

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

        collection.replaceOne(eq("_id", objectId), toDocument(location, objectId), new ReplaceOptions().upsert(false));
        return location;
    }

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

    private Location toLocation(Document document, Map<String, com.example.mongocrud.trip.Trip> tripsById) {
        ObjectId id = document.getObjectId("_id");
        ObjectId tripId = document.getObjectId("tripId");
        return new Location(
                id == null ? null : id.toHexString(),
                tripId == null ? null : tripsById.get(tripId.toHexString()),
                toBigDecimal(document.get("latitude")),
                toBigDecimal(document.get("longitude")),
                toInstant(document.getDate("recordedAt")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
