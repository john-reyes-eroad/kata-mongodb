package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractBlackboxTest {

    @BeforeAll
    static void configureBaseUrl() {
        String fromProperty = System.getProperty("api.baseUrl");
        String fromEnv = System.getenv("API_BASE_URL");
        RestAssured.baseURI = fromProperty != null && !fromProperty.isBlank()
                ? fromProperty
                : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:8080");
    }

    protected record TripFixture(String tripId, String vehicleId, String driverId) {
    }

    protected String randomSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createVehicle(String suffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vin", "VIN-" + suffix);
        payload.put("make", "COVESA");
        payload.put("model", "Telematics-" + suffix);
        payload.put("year", 2024);

        return given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    protected String createDriver(String suffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Driver " + suffix);
        payload.put("licenseNumber", "LIC-" + suffix);

        return given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    protected String createTrip(String vehicleId, String driverId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("driverId", driverId);
        payload.put("startTime", Instant.now().minusSeconds(3600).toString());
        payload.put("endTime", Instant.now().toString());
        payload.put("distanceKm", new BigDecimal("42.5"));

        return given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    protected TripFixture createTripFixture(String suffix) {
        String vehicleId = createVehicle(suffix);
        String driverId = createDriver(suffix);
        String tripId = createTrip(vehicleId, driverId);
        return new TripFixture(tripId, vehicleId, driverId);
    }
}

