package com.example.mongocrud.vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import static com.example.mongocrud.common.MongoSupport.parseObjectId;
import static com.example.mongocrud.common.MongoSupport.toDate;
import static com.example.mongocrud.common.MongoSupport.toInstant;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class VehicleRepository {

    private final MongoCollection<Document> collection;

    public VehicleRepository(MongoDatabase database) {
        this.collection = database.getCollection("vehicles");
    }

    public List<Vehicle> findAll() {
        return collection.find().map(this::toVehicle).into(new ArrayList<>());
    }

    public Optional<Vehicle> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        return document == null ? Optional.empty() : Optional.of(toVehicle(document));
    }

    public Map<String, Vehicle> findByIds(Collection<String> ids) {
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
        LinkedHashMap<String, Vehicle> vehiclesById = new LinkedHashMap<>();
        for (Document document : documents) {
            Vehicle vehicle = toVehicle(document);
            if (vehicle.id() != null) {
                vehiclesById.put(vehicle.id(), vehicle);
            }
        }
        return vehiclesById;
    }

    public List<Vehicle> search(String keyword) {
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.or(
                Filters.regex("vin", pattern),
                Filters.regex("make", pattern),
                Filters.regex("model", pattern));
        return collection.find(filter).map(this::toVehicle).into(new ArrayList<>());
    }

    public Vehicle save(Vehicle vehicle) {
        ObjectId objectId = parseObjectId(vehicle.id());
        if (objectId == null) {
            objectId = new ObjectId();
            Vehicle created = new Vehicle(
                    objectId.toHexString(),
                    vehicle.vin(),
                    vehicle.make(),
                    vehicle.model(),
                    vehicle.year(),
                    vehicle.createdAt(),
                    vehicle.updatedAt()
            );
            collection.insertOne(toDocument(created, objectId));
            return created;
        }

        collection.replaceOne(eq("_id", objectId), toDocument(vehicle, objectId), new ReplaceOptions().upsert(false));
        return vehicle;
    }

    public void delete(Vehicle vehicle) {
        ObjectId objectId = parseObjectId(vehicle.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(Vehicle vehicle, ObjectId id) {
        return new Document("_id", id)
                .append("vin", vehicle.vin())
                .append("make", vehicle.make())
                .append("model", vehicle.model())
                .append("year", vehicle.year())
                .append("createdAt", toDate(vehicle.createdAt()))
                .append("updatedAt", toDate(vehicle.updatedAt()));
    }

    private Vehicle toVehicle(Document document) {
        ObjectId id = document.getObjectId("_id");
        Number year = document.get("year", Number.class);
        return new Vehicle(
                id == null ? null : id.toHexString(),
                document.getString("vin"),
                document.getString("make"),
                document.getString("model"),
                year == null ? 0 : year.intValue(),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
