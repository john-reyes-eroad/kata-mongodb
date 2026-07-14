package com.example.mongocrud.trip;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.mongocrud.driver.Driver;
import com.example.mongocrud.vehicle.Vehicle;
public record Trip(
        String id,
        Vehicle vehicle,
        Driver driver,
        Instant startTime,
        Instant endTime,
        BigDecimal distanceKm,
        Instant createdAt,
        Instant updatedAt
) {
}
