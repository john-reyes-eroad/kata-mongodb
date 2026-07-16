package com.example.mongocrud.trip.application;

import java.math.BigDecimal;
import java.time.Instant;

public record TripUpsertCommand(
        String vehicleId,
        String driverId,
        Instant startTime,
        Instant endTime,
        BigDecimal distanceKm
) {
}
