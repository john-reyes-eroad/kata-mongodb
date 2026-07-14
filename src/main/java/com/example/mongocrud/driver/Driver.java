package com.example.mongocrud.driver;

import java.time.Instant;

public record Driver(
        String id,
        String name,
        String licenseNumber,
        Instant createdAt,
        Instant updatedAt
) {
}
