package com.example.mongodb.adapter.inbound.ratelimit;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("rate-limit")
public record RateLimitProperties(
        @DefaultValue("10") int requestsPerSecond,
        @DefaultValue("10000") long maximumClients,
        @DefaultValue("PT10M") Duration clientIdleTimeout
) {

    public RateLimitProperties {
        if (requestsPerSecond <= 0) {
            throw new IllegalArgumentException("rate-limit.requests-per-second must be positive");
        }
        if (maximumClients <= 0) {
            throw new IllegalArgumentException("rate-limit.maximum-clients must be positive");
        }
        if (clientIdleTimeout.isNegative() || clientIdleTimeout.isZero()) {
            throw new IllegalArgumentException("rate-limit.client-idle-timeout must be positive");
        }
    }
}
