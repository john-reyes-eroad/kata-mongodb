package com.example.mongocrud.driver;

import jakarta.validation.constraints.NotBlank;

public record DriverRequest(
        @NotBlank String name,
        @NotBlank String licenseNumber
) {
}

