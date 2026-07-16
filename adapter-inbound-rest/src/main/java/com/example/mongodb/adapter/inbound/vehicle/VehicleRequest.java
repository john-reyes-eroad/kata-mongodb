package com.example.mongodb.adapter.inbound.vehicle;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record VehicleRequest(
        @NotBlank String vin,
        @NotBlank String make,
        @NotBlank String model,
        @Min(1900) @Max(2100) int year
) {
}
