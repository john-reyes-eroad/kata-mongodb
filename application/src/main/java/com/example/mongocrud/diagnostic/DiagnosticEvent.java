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
    public DiagnosticEvent(
            Vehicle vehicle,
            String code,
            String severity,
            String description,
            Instant occurredAt,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(null, vehicle, code, severity, description, occurredAt, createdAt, updatedAt);
    }

    public DiagnosticEvent(String id) {
        this(id, null, null, null, null, null, null, null);
    }
}
