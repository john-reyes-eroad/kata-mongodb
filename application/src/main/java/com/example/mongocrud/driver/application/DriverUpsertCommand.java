package com.example.mongocrud.driver.application;

public record DriverUpsertCommand(
        String name,
        String licenseNumber
) {
}
