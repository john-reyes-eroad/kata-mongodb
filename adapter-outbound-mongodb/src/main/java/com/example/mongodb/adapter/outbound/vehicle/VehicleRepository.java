package com.example.mongodb.adapter.outbound.vehicle;

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
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.port.outbound.VehiclePersistencePort;
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
public class VehicleRepository implements VehiclePersistencePort {

    private final MongoCollection<Document> collection;

    public VehicleRepository(MongoDatabase database) {
        this.collection = database.getCollection("vehicles");
    }

    @Override
    public List<Vehicle> findAll() {
        return collection.find().map(this::toVehicle).into(new ArrayList<>());
    }

    @Override
    public Optional<Vehicle> findById(String id) {
        var objectId = parseObjectId(id);

        if (objectId == null) {
            return Optional.empty();
        }

        var document = collection
            .find(eq("_id", objectId))
            .first();

        return document == null
            ? Optional.empty()
            : Optional.of(toVehicle(document));
    }

    @Override
    public Map<String, Vehicle> findByIds(Collection<String> ids) {
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

        var vehiclesById = new LinkedHashMap<String, Vehicle>();
        for (var document : documents) {
            var vehicle = toVehicle(document);
            if (vehicle.id() != null) {
                vehiclesById.put(vehicle.id(), vehicle);
            }
        }

        return vehiclesById;
    }

    @Override
    public List<Vehicle> search(String keyword) {
        return collection
            .find(keywordFilter(keyword))
            .map(this::toVehicle)
            .into(new ArrayList<>());
    }

    @Override
    public long count(String keyword) {
        return keyword == null || keyword.isBlank()
            ? collection.countDocuments()
            : collection.countDocuments(keywordFilter(keyword));
    }

    @Override
    public Vehicle save(Vehicle vehicle) {
        try {
            var objectId = parseObjectId(vehicle.id());

            if (objectId == null) {
                objectId = new ObjectId();
                var vehicleCreated = new Vehicle(
                    objectId.toHexString(),
                    vehicle.vin(),
                    vehicle.make(),
                    vehicle.model(),
                    vehicle.year(),
                    vehicle.createdAt(),
                    vehicle.updatedAt()
                );
                collection
                    .insertOne(toDocument(vehicleCreated, objectId));
                return vehicleCreated;
            }

            var updateResult = collection
                .replaceOne(
                    eq("_id", objectId),
                    toDocument(vehicle, objectId),
                    new ReplaceOptions().upsert(false)
                );

            if (updateResult.getMatchedCount() == 0) {
                throw new ResourceNotFoundException("Vehicle not found: " + vehicle.id());
            }

            return vehicle;
        } catch (MongoWriteException ex) {
            if (ex.getError().getCategory() != ErrorCategory.DUPLICATE_KEY) {
                throw ex;
            }
            throw new DuplicateResourceException("Vehicle VIN must be unique");
        }
    }

    @Override
    public void delete(Vehicle vehicle) {
        var objectId = parseObjectId(vehicle.id());
        if (objectId != null) {
            collection
                .deleteOne(eq("_id", objectId));
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

    private Bson keywordFilter(String keyword) {
        var pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Filters.or(
            Filters.regex("vin", pattern),
            Filters.regex("make", pattern),
            Filters.regex("model", pattern)
        );
    }

    private Vehicle toVehicle(Document document) {
        var id = document.getObjectId("_id");
        var year = document.get("year", Number.class);
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
