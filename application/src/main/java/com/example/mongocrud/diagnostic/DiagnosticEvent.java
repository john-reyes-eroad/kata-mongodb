package com.example.mongocrud.diagnostic;

import java.time.Instant;

import com.example.mongocrud.vehicle.Vehicle;
public record DiagnosticEvent(
        String id,
        Vehicle vehicle,
        String code,
        String severity,
        String description,
        Instant occurredAt,
        Instant createdAt,
        Instant updatedAt
) {
}
