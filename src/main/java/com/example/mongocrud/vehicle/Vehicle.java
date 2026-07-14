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
}
