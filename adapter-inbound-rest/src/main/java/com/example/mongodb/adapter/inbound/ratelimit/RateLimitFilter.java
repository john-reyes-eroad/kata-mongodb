package com.example.mongodb.adapter.inbound.ratelimit;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public final class RateLimitFilter implements Filter {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final Duration REFILL_PERIOD = Duration.ofSeconds(1);

    private final RateLimitProperties properties;
    private final Cache<String, Bucket> clientBuckets;

    RateLimitFilter(RateLimitProperties properties) {
        this.properties = properties;
        this.clientBuckets = Caffeine.<String, Bucket>newBuilder()
                .maximumSize(properties.maximumClients())
                .expireAfterAccess(properties.clientIdleTimeout())
                .build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest httpRequest)
                || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        var bucket = clientBuckets.get(resolveClientIdentity(httpRequest), ignored -> newBucket());
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        applyRateLimitHeaders(httpResponse, probe);

        if (probe.isConsumed()) {
            chain.doFilter(request, response);
            return;
        }

        var retryAfterSeconds = secondsUntil(probe.getNanosToWaitForRefill());
        httpResponse.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        httpResponse.setStatus(TOO_MANY_REQUESTS);
        httpResponse.setContentType("application/json");
        httpResponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        httpResponse.getWriter().write(
                "{\"code\":\"too_many_requests\",\"message\":\"Rate limit exceeded. Retry after "
                        + retryAfterSeconds + " seconds.\"}"
        );
    }

    private Bucket newBucket() {
        var limit = Bandwidth.builder()
                .capacity(properties.requestsPerSecond())
                .refillIntervally(properties.requestsPerSecond(), REFILL_PERIOD)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private void applyRateLimitHeaders(HttpServletResponse response, ConsumptionProbe probe) {
        response.setHeader("RateLimit-Limit", Integer.toString(properties.requestsPerSecond()));
        response.setHeader("RateLimit-Remaining", Long.toString(probe.getRemainingTokens()));
        response.setHeader(
                "RateLimit-Reset",
                Long.toString(probe.getRemainingTokens() == 0 ? secondsUntil(REFILL_PERIOD.toNanos()) : 0)
        );
    }

    private long secondsUntil(long nanos) {
        if (nanos <= 0) {
            return 0;
        }
        return Math.max(1, TimeUnit.NANOSECONDS.toSeconds(nanos - 1) + 1);
    }

    private String resolveClientIdentity(HttpServletRequest request) {
        var remoteAddress = request.getRemoteAddr();
        return remoteAddress == null || remoteAddress.isBlank() ? "unknown" : remoteAddress;
    }
}
