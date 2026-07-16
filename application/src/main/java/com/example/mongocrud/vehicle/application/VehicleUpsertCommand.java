package com.example.mongocrud.vehicle.application;

public record VehicleUpsertCommand(
        String vin,
        String make,
        String model,
        int year
) {
}
