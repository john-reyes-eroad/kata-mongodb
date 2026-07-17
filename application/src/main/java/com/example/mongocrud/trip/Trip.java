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
    public Trip(
            Vehicle vehicle,
            Driver driver,
            Instant startTime,
            Instant endTime,
            BigDecimal distanceKm,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(null, vehicle, driver, startTime, endTime, distanceKm, createdAt, updatedAt);
    }

    public Trip(String id) {
        this(id, null, null, null, null, null, null, null);
    }
}
