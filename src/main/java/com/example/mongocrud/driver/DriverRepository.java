package com.example.mongocrud.driver;

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
public class DriverRepository {

    private final MongoCollection<Document> collection;

    public DriverRepository(MongoDatabase database) {
        this.collection = database.getCollection("drivers");
    }

    public List<Driver> findAll() {
        return collection.find().map(this::toDriver).into(new ArrayList<>());
    }

    public Optional<Driver> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        return document == null ? Optional.empty() : Optional.of(toDriver(document));
    }

    public Map<String, Driver> findByIds(Collection<String> ids) {
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
        LinkedHashMap<String, Driver> driversById = new LinkedHashMap<>();
        for (Document document : documents) {
            Driver driver = toDriver(document);
            if (driver.id() != null) {
                driversById.put(driver.id(), driver);
            }
        }
        return driversById;
    }

    public List<Driver> search(String keyword) {
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE);
        Bson filter = Filters.or(
                Filters.regex("name", pattern),
                Filters.regex("licenseNumber", pattern));
        return collection.find(filter).map(this::toDriver).into(new ArrayList<>());
    }

    public Driver save(Driver driver) {
        ObjectId objectId = parseObjectId(driver.id());
        if (objectId == null) {
            objectId = new ObjectId();
            Driver created = new Driver(
                    objectId.toHexString(),
                    driver.name(),
                    driver.licenseNumber(),
                    driver.createdAt(),
                    driver.updatedAt()
            );
            collection.insertOne(toDocument(created, objectId));
            return created;
        }

        collection.replaceOne(eq("_id", objectId), toDocument(driver, objectId), new ReplaceOptions().upsert(false));
        return driver;
    }

    public void delete(Driver driver) {
        ObjectId objectId = parseObjectId(driver.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(Driver driver, ObjectId id) {
        return new Document("_id", id)
                .append("name", driver.name())
                .append("licenseNumber", driver.licenseNumber())
                .append("createdAt", toDate(driver.createdAt()))
                .append("updatedAt", toDate(driver.updatedAt()));
    }

    private Driver toDriver(Document document) {
        ObjectId id = document.getObjectId("_id");
        return new Driver(
                id == null ? null : id.toHexString(),
                document.getString("name"),
                document.getString("licenseNumber"),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
