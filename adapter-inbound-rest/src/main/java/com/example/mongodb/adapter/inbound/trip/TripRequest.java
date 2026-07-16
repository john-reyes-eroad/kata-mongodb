package com.example.mongodb.adapter.inbound.trip;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TripRequest(
        @NotBlank String vehicleId,
        @NotBlank String driverId,
        @NotNull Instant startTime,
        Instant endTime,
        @NotNull @PositiveOrZero BigDecimal distanceKm
) {
}
