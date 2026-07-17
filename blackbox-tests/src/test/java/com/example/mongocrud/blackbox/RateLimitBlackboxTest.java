package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RateLimitBlackboxTest extends AbstractBlackboxTest {

    private static final int REQUESTS_PER_SECOND = 50;

    @BeforeEach
    void disableRequestPacing() throws InterruptedException {
        setRateLimitPacingDisabled(true);
        Thread.sleep(1_100);
    }

    @AfterEach
    void enableRequestPacing() {
        setRateLimitPacingDisabled(false);
    }

    @Test
    void shouldAllowTenConcurrentRequestsAndRejectFurtherRequests() throws Exception {
        List<Response> responses = sendConcurrentRequests(REQUESTS_PER_SECOND + 2);

        long successfulRequests = responses.stream().filter(response -> response.statusCode() == 200).count();
        List<Response> rejectedRequests = responses.stream()
                .filter(response -> response.statusCode() == 429)
                .toList();

        assertEquals(REQUESTS_PER_SECOND, successfulRequests);
        assertEquals(2, rejectedRequests.size());
        for (Response rejectedRequest : rejectedRequests) {
            assertEquals(String.valueOf(REQUESTS_PER_SECOND), rejectedRequest.getHeader("RateLimit-Limit"));
            assertEquals("0", rejectedRequest.getHeader("RateLimit-Remaining"));
            assertTrue(Long.parseLong(rejectedRequest.getHeader("Retry-After")) >= 1);
            assertEquals("too_many_requests", rejectedRequest.jsonPath().getString("code"));
        }
    }

    @Test
    void shouldIgnoreForwardedAddressAndNotLimitActuator() throws Exception {
        List<Response> responses = sendConcurrentRequests(REQUESTS_PER_SECOND + 1, true);

        assertEquals(REQUESTS_PER_SECOND, responses.stream().filter(response -> response.statusCode() == 200).count());
        assertEquals(1, responses.stream().filter(response -> response.statusCode() == 429).count());

        given()
                .header("X-Forwarded-For", "198.51.100.1")
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200);
    }

    private List<Response> sendConcurrentRequests(int requestCount) throws Exception {
        return sendConcurrentRequests(requestCount, false);
    }

    private List<Response> sendConcurrentRequests(int requestCount, boolean useSpoofedForwardedAddresses) throws Exception {
        CountDownLatch ready = new CountDownLatch(requestCount);
        CountDownLatch start = new CountDownLatch(1);
        try (ExecutorService executor = java.util.concurrent.Executors.newFixedThreadPool(requestCount)) {
            List<Future<Response>> futures = new ArrayList<>();
            for (int request = 0; request < requestCount; request++) {
                String forwardedAddress = "203.0.113." + request;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    var requestSpecification = given();
                    if (useSpoofedForwardedAddresses) {
                        requestSpecification.header("X-Forwarded-For", forwardedAddress);
                    }
                    return requestSpecification.when().get("/api/vehicles");
                }));
            }
            ready.await();
            start.countDown();

            List<Response> responses = new ArrayList<>();
            for (Future<Response> future : futures) {
                responses.add(future.get());
            }
            return responses;
        }
    }

}
