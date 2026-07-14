package com.example.mongocrud.trip;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.driver.DriverRepository;
import com.example.mongocrud.vehicle.Vehicle;
import com.example.mongocrud.vehicle.VehicleRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
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
public class TripRepository {

    private final MongoCollection<Document> collection;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    public TripRepository(MongoDatabase database, VehicleRepository vehicleRepository, DriverRepository driverRepository) {
        this.collection = database.getCollection("trips");
        this.vehicleRepository = vehicleRepository;
        this.driverRepository = driverRepository;
    }

    public List<Trip> findAll() {
        List<Document> documents = collection.find().into(new ArrayList<>());
        return mapDocumentsToTrips(documents);
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
        List<Trip> trips = mapDocumentsToTrips(List.of(document));
        return trips.isEmpty() ? Optional.empty() : Optional.of(trips.getFirst());
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
        List<Trip> trips = mapDocumentsToTrips(documents);
        LinkedHashMap<String, Trip> tripsById = new LinkedHashMap<>();
        for (Trip trip : trips) {
            if (trip.id() != null) {
                tripsById.put(trip.id(), trip);
            }
        }
        return tripsById;
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

        collection.replaceOne(eq("_id", objectId), toDocument(trip, objectId), new ReplaceOptions().upsert(false));
        return trip;
    }

    public void delete(Trip trip) {
        ObjectId objectId = parseObjectId(trip.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
        }
    }

    private List<Trip> mapDocumentsToTrips(List<Document> documents) {
        LinkedHashSet<String> vehicleIds = new LinkedHashSet<>();
        LinkedHashSet<String> driverIds = new LinkedHashSet<>();

        for (Document document : documents) {
            ObjectId vehicleId = document.getObjectId("vehicleId");
            if (vehicleId != null) {
                vehicleIds.add(vehicleId.toHexString());
            }
            ObjectId driverId = document.getObjectId("driverId");
            if (driverId != null) {
                driverIds.add(driverId.toHexString());
            }
        }

        Map<String, Vehicle> vehiclesById = vehicleRepository.findByIds(vehicleIds);
        Map<String, Driver> driversById = driverRepository.findByIds(driverIds);

        List<Trip> trips = new ArrayList<>(documents.size());
        for (Document document : documents) {
            trips.add(toTrip(document, vehiclesById, driversById));
        }
        return trips;
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

    private Trip toTrip(Document document, Map<String, Vehicle> vehiclesById, Map<String, Driver> driversById) {
        ObjectId id = document.getObjectId("_id");
        ObjectId vehicleId = document.getObjectId("vehicleId");
        ObjectId driverId = document.getObjectId("driverId");
        return new Trip(
                id == null ? null : id.toHexString(),
                vehicleId == null ? null : vehiclesById.get(vehicleId.toHexString()),
                driverId == null ? null : driversById.get(driverId.toHexString()),
                toInstant(document.getDate("startTime")),
                toInstant(document.getDate("endTime")),
                toBigDecimal(document.get("distanceKm")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
