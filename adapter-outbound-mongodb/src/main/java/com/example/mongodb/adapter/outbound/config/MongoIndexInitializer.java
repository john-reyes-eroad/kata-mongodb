package com.example.mongodb.adapter.outbound.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MongoIndexInitializer {

    private final MongoDatabase database;

    public MongoIndexInitializer(MongoDatabase database) {
        this.database = database;
    }

    @PostConstruct
    public void ensureIndexes() {
        database.getCollection("vehicles")
            .createIndex(
                Indexes.ascending("vin"),
                new IndexOptions()
                    .name("vin")
                    .unique(true)
            );
        database.getCollection("drivers")
            .createIndex(
                Indexes.ascending("name"),
                new IndexOptions()
                    .name("name")
                    .unique(true)
            );
        database.getCollection("drivers")
            .createIndex(
                Indexes.ascending("licenseNumber"),
                new IndexOptions()
                    .name("licenseNumber")
                    .unique(true)
            );
    }
}
