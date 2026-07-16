package com.example.mongocrud.location;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.mongocrud.trip.Trip;
public record Location(
        String id,
        Trip trip,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant recordedAt,
        Instant createdAt,
        Instant updatedAt
) {
}
