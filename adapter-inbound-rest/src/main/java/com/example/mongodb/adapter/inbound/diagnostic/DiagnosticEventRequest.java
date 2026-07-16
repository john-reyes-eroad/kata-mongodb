package com.example.mongodb.adapter.inbound.diagnostic;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosticEventRequest(
        @NotBlank String vehicleId,
        @NotBlank String code,
        @NotBlank String severity,
        @NotBlank String description,
        @NotNull Instant occurredAt
) {
}
