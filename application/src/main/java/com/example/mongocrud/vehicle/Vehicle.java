package com.example.mongocrud.vehicle;

import java.time.Instant;

public record Vehicle(
        String id,
        String vin,
        String make,
        String model,
        int year,
        Instant createdAt,
        Instant updatedAt
) {
    public Vehicle(
            String vin,
            String make,
            String model,
            int year,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(null, vin, make, model, year, createdAt, updatedAt);
    }

    public Vehicle(String id) {
        this(id, null, null, null, 0, null, null);
    }
}
