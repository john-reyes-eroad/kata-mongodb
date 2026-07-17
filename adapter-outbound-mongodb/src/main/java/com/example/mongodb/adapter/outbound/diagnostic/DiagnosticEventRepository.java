package com.example.mongodb.adapter.outbound.diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.diagnostic.DiagnosticEvent;
import com.example.mongocrud.diagnostic.port.outbound.DiagnosticEventPersistencePort;
import com.example.mongocrud.vehicle.Vehicle;
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
public class DiagnosticEventRepository implements DiagnosticEventPersistencePort {

    private final MongoCollection<Document> collection;

    public DiagnosticEventRepository(MongoDatabase database) {
        this.collection = database.getCollection("diagnostic_events");
    }

    @Override
    public List<DiagnosticEvent> findAll() {
        return collection.find().map(this::toDiagnosticEvent).into(new ArrayList<>());
    }

    @Override
    public Optional<DiagnosticEvent> findById(String id) {
        ObjectId objectId = parseObjectId(id);
        if (objectId == null) {
            return Optional.empty();
        }
        Document document = collection.find(eq("_id", objectId)).first();
        if (document == null) {
            return Optional.empty();
        }
        return Optional.of(toDiagnosticEvent(document));
    }

    @Override
    public long count(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return collection.countDocuments();
        }

        Bson filter = keywordFilter(keyword);
        ObjectId objectId = parseObjectId(keyword);
        if (objectId != null) {
            filter = Filters.or(
                    filter,
                    eq("_id", objectId),
                    eq("vehicleId", objectId));
        }
        return collection.countDocuments(filter);
    }

    @Override
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

        if (collection.replaceOne(eq("_id", objectId), toDocument(event, objectId),
                new ReplaceOptions().upsert(false)).getMatchedCount() == 0) {
            throw new ResourceNotFoundException("Diagnostic event not found: " + event.id());
        }
        return event;
    }

    @Override
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

    private Bson keywordFilter(String keyword) {
        Pattern pattern = Pattern.compile(Pattern.quote(keyword), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return Filters.or(
                Filters.regex("code", pattern),
                Filters.regex("severity", pattern),
                Filters.regex("description", pattern));
    }

    private DiagnosticEvent toDiagnosticEvent(Document document) {
        ObjectId id = document.getObjectId("_id");
        String hexId = id == null ? null : id.toHexString();
        ObjectId vehicleId = document.getObjectId("vehicleId");

        return new DiagnosticEvent(
                hexId,
                vehicleId == null ? null : new Vehicle(vehicleId.toHexString()),
                document.getString("code"),
                document.getString("severity"),
                document.getString("description"),
                toInstant(document.getDate("occurredAt")),
                toInstant(document.getDate("createdAt")),
                toInstant(document.getDate("updatedAt"))
        );
    }
}
