package com.example.mongodb.adapter.inbound.driver;

import jakarta.validation.constraints.NotBlank;

public record DriverRequest(
        @NotBlank String name,
        @NotBlank String licenseNumber
) {
}
