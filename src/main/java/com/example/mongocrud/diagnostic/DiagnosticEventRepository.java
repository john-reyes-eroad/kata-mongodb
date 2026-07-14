package com.example.mongocrud.diagnostic;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.mongocrud.vehicle.VehicleRepository;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Repository;

import static com.example.mongocrud.common.MongoSupport.parseObjectId;
import static com.example.mongocrud.common.MongoSupport.toDate;
import static com.example.mongocrud.common.MongoSupport.toInstant;
import static com.mongodb.client.model.Filters.eq;

@Repository
public class DiagnosticEventRepository {

    private final MongoCollection<Document> collection;
    private final VehicleRepository vehicleRepository;

    public DiagnosticEventRepository(MongoDatabase database, VehicleRepository vehicleRepository) {
        this.collection = database.getCollection("diagnostic_events");
        this.vehicleRepository = vehicleRepository;
    }

    public List<DiagnosticEvent> findAll() {
        List<Document> documents = collection.find().into(new ArrayList<>());
        LinkedHashSet<String> vehicleIds = new LinkedHashSet<>();
        for (Document document : documents) {
            ObjectId vehicleId = document.getObjectId("vehicleId");
            if (vehicleId != null) {
                vehicleIds.add(vehicleId.toHexString());
            }
        }
        Map<String, com.example.mongocrud.vehicle.Vehicle> vehiclesById = vehicleRepository.findByIds(vehicleIds);

        List<DiagnosticEvent> events = new ArrayList<>(documents.size());
        for (Document document : documents) {
            events.add(toDiagnosticEvent(document, vehiclesById));
        }
        return events;
    }

    public Optional<DiagnosticEvent> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        if (document == null) {
            return Optional.empty();
        }
        ObjectId vehicleId = document.getObjectId("vehicleId");
        Map<String, com.example.mongocrud.vehicle.Vehicle> vehiclesById = vehicleId == null
                ? Map.of()
                : vehicleRepository.findByIds(List.of(vehicleId.toHexString()));
        return Optional.of(toDiagnosticEvent(document, vehiclesById));
    }

    public DiagnosticEvent save(DiagnosticEvent event) {
        ObjectId objectId = parseObjectId(event.id());
        if (objectId == null) {
            objectId = new ObjectId();
            DiagnosticEvent created = new DiagnosticEvent(
                    objectId.toHexString(),
                    event.vehicle(),
                    event.code(),
                    event.severity(),
                    event.description(),
                    event.occurredAt(),
                    event.createdAt(),
                    event.updatedAt()
            );
            collection.insertOne(toDocument(created, objectId));
            return created;
        }

        collection.replaceOne(eq("_id", objectId), toDocument(event, objectId), new ReplaceOptions().upsert(false));
        return event;
    }

    public void delete(DiagnosticEvent event) {
        ObjectId objectId = parseObjectId(event.id());
        if (objectId != null) {
            collection.deleteOne(eq("_id", objectId));
        }
    }

    private Document toDocument(DiagnosticEvent event, ObjectId id) {
        return new Document("_id", id)
                .append("vehicleId", parseObjectId(event.vehicle() == null ? null : event.vehicle().id()))
                .append("code", event.code())
                .append("severity", event.severity())
                .append("description", event.description())
                .append("occurredAt", toDate(event.occurredAt()))
                .append("createdAt", toDate(event.createdAt()))
                .append("updatedAt", toDate(event.updatedAt()));
    }

    private DiagnosticEvent toDiagnosticEvent(
            Document document,
            Map<String, com.example.mongocrud.vehicle.Vehicle> vehiclesById) {
        ObjectId id = document.getObjectId("_id");
        String hexId = id == null ? null : id.toHexString();
        ObjectId vehicleId = document.getObjectId("vehicleId");

        return new DiagnosticEvent(
                hexId,
                vehicleId == null ? null : vehiclesById.get(vehicleId.toHexString()),
                document.getString("code"),
                document.getString("severity"),
                document.getString("description"),
                toInstant(document.getDate("occurredAt")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
