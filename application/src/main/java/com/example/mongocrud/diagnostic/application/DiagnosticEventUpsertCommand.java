package com.example.mongocrud.diagnostic.application;

import java.time.Instant;

public record DiagnosticEventUpsertCommand(
        String vehicleId,
        String code,
        String severity,
        String description,
        Instant occurredAt
) {
}
