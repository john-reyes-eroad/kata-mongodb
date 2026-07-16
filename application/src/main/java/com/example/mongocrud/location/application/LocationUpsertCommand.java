package com.example.mongocrud.location.application;

import java.math.BigDecimal;
import java.time.Instant;

public record LocationUpsertCommand(
        String tripId,
        BigDecimal latitude,
        BigDecimal longitude,
        Instant recordedAt
) {
}
