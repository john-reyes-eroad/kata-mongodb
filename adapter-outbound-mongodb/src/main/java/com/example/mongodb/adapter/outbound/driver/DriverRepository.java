package com.example.mongodb.adapter.outbound.driver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.mongocrud.common.DuplicateResourceException;
import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.port.outbound.DriverPersistencePort;
import com.mongodb.ErrorCategory;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import static com.example.mongodb.adapter.outbound.common.MongoSupport.parseObjectId;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toDate;
import static com.example.mongodb.adapter.outbound.common.MongoSupport.toInstant;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class DriverRepository implements DriverPersistencePort {

    private final MongoCollection<Document> collection;

    public DriverRepository(MongoDatabase database) {
        this.collection = database.getCollection("drivers");
    }

    @Override
    public List<Driver> findAll() {
        return collection.find().map(this::toDriver).into(new ArrayList<>());
    }

    @Override
    public Optional<Driver> findById(String id) {
        var objectId = parseObjectId(id);

        if (objectId == null) {
            return Optional.empty();
        }

        var document = collection
            .find(eq("_id", objectId))
            .first();

        return document == null
            ? Optional.empty()
            : Optional.of(toDriver(document));
    }

    @Override
    public Map<String, Driver> findByIds(Collection<String> ids) {
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

        var driversById = new LinkedHashMap<String, Driver>();
        for (var document : documents) {
            var driver = toDriver(document);
            if (driver.id() != null) {
                driversById.put(driver.id(), driver);
            }
        }

        return driversById;
    }

    @Override
    public List<Driver> search(String keyword) {
        return collection.find(keywordFilter(keyword)).map(this::toDriver).into(new ArrayList<>());
    }

    @Override
    public long count(String keyword) {
        return keyword == null || keyword.isBlank()
            ? collection.countDocuments()
            : collection.countDocuments(keywordFilter(keyword));
    }

    @Override
    public Driver save(Driver driver) {
        try {
            var objectId = parseObjectId(driver.id());

            if (objectId == null) {
                objectId = new ObjectId();
                var created = new Driver(
                    objectId.toHexString(),
                    driver.name(),
                    driver.licenseNumber(),
                    driver.createdAt(),
                    driver.updatedAt()
                );
                collection
                    .insertOne(toDocument(created, objectId));
                return created;
            }

            var updateResult = collection
                .replaceOne(
                    eq("_id", objectId),
                    toDocument(driver, objectId),
                    new ReplaceOptions().upsert(false)
                );

            if (updateResult.getMatchedCount() == 0) {
                throw new ResourceNotFoundException("Driver not found: " + driver.id());
            }

            return driver;
        } catch (MongoWriteException ex) {
            if (ex.getError() == null || ex.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
                throw ex;
            }
            throw new DuplicateResourceException(duplicateMessage(ex));
        }
    }

    private String duplicateMessage(MongoWriteException ex) {
        var message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        return message.contains("licensenumber")
            ? "Driver license number must be unique"
            : "Driver name must be unique";
    }

    @Override
    public void delete(Driver driver) {
        var objectId = parseObjectId(driver.id());
        if (objectId != null) {
            collection
                .deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(Driver driver, ObjectId id) {
        return new Document("_id", id)
            .append("name", driver.name())
            .append("licenseNumber", driver.licenseNumber())
            .append("createdAt", toDate(driver.createdAt()))
            .append("updatedAt", toDate(driver.updatedAt()));
    }

    private Bson keywordFilter(String keyword) {
        var pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Filters.or(
            Filters.regex("name", pattern),
            Filters.regex("licenseNumber", pattern)
        );
    }

    private Driver toDriver(Document document) {
        var id = document.getObjectId("_id");
        return new Driver(
            id == null ? null : id.toHexString(),
            document.getString("name"),
            document.getString("licenseNumber"),
            toInstant(document.getDate("createdAt")),
            toInstant(document.getDate("updatedAt"))
        );
    }
}
