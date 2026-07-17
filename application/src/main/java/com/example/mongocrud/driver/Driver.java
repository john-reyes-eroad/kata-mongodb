package com.example.mongocrud.driver;

import java.time.Instant;

public record Driver(
        String id,
        String name,
        String licenseNumber,
        Instant createdAt,
        Instant updatedAt
) {
    public Driver(
            String name,
            String licenseNumber,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(null, name, licenseNumber, createdAt, updatedAt);
    }

    public Driver(String id) {
        this(id, null, null, null, null);
    }
}
