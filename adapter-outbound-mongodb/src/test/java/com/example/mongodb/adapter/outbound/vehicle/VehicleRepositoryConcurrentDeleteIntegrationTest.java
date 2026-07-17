package com.example.mongodb.adapter.outbound.vehicle;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.UUID;

import com.example.mongocrud.common.ResourceNotFoundException;
import com.example.mongocrud.vehicle.Vehicle;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "mongodb.integration.tests", matches = "true")
class VehicleRepositoryConcurrentDeleteIntegrationTest {

    @Test
    void updateShouldReturnNotFoundWhenRecordIsDeletedAfterItsPreliminaryRead() {
        try (MongoClient client = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = client.getDatabase("kata_mongodb");
            VehicleRepository repository = new VehicleRepository(database);
            String suffix = UUID.randomUUID().toString();
            Instant now = Instant.now();
            Vehicle created = repository.save(new Vehicle(
                    null, "race-" + suffix, "make", "model", 2024, now, now));

            try {
                Vehicle existing = repository.findById(created.id()).orElseThrow();
                repository.delete(existing);

                Vehicle updated = new Vehicle(
                        existing.id(), "race-updated-" + suffix, "make", "model", 2025,
                        existing.createdAt(), Instant.now());

                assertThrows(ResourceNotFoundException.class, () -> repository.save(updated));
            } finally {
                database.getCollection("vehicles").deleteOne(
                        new org.bson.Document("_id", new org.bson.types.ObjectId(created.id())));
            }
        }
    }
}
