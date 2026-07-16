package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractBlackboxTest {

    private final List<TestResource> createdResources = new ArrayList<>();

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

    private record TestResource(String domainPath, String id) {
    }

    protected String randomSuffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    protected String createVehicle(String suffix) {
        return createVehicle(vehiclePayload(suffix));
    }

    protected Map<String, Object> vehiclePayload(String suffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vin", "VIN-" + suffix);
        payload.put("make", "Make-" + suffix);
        payload.put("model", "Model-" + suffix);
        payload.put("year", 2024);
        return payload;
    }

    protected String createVehicle(Map<String, Object> payload) {
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/vehicles", id);
        return id;
    }

    protected String createDriver(String suffix) {
        return createDriver(driverPayload(suffix));
    }

    protected Map<String, Object> driverPayload(String suffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Driver " + suffix);
        payload.put("licenseNumber", "LIC-" + suffix);
        return payload;
    }

    protected String createDriver(Map<String, Object> payload) {
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/drivers", id);
        return id;
    }

    protected String createTrip(String vehicleId, String driverId) {
        return createTrip(tripPayload(vehicleId, driverId));
    }

    protected Map<String, Object> tripPayload(String vehicleId, String driverId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("driverId", driverId);
        payload.put("startTime", Instant.now().minusSeconds(3600).toString());
        payload.put("endTime", Instant.now().toString());
        payload.put("distanceKm", new BigDecimal("42.5"));
        return payload;
    }

    protected String createTrip(Map<String, Object> payload) {
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/trips", id);
        return id;
    }

    protected TripFixture createTripFixture(String suffix) {
        String vehicleId = createVehicle(suffix);
        String driverId = createDriver(suffix);
        String tripId = createTrip(vehicleId, driverId);
        return new TripFixture(tripId, vehicleId, driverId);
    }

    protected Map<String, Object> locationPayload(String tripId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tripId", tripId);
        payload.put("latitude", new BigDecimal("-36.8485"));
        payload.put("longitude", new BigDecimal("174.7633"));
        payload.put("recordedAt", Instant.now().toString());
        return payload;
    }

    protected String createLocation(Map<String, Object> payload) {
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/locations")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/locations", id);
        return id;
    }

    protected Map<String, Object> diagnosticEventPayload(String vehicleId, String suffix) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("code", "P0001-" + suffix);
        payload.put("severity", "HIGH-" + suffix);
        payload.put("description", "Diagnostic event " + suffix);
        payload.put("occurredAt", Instant.now().toString());
        return payload;
    }

    protected String createDiagnosticEvent(Map<String, Object> payload) {
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/diagnostic-events")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/diagnostic-events", id);
        return id;
    }

    protected void trackResource(String domainPath, String id) {
        createdResources.add(new TestResource(domainPath, id));
    }

    protected void deleteResource(String domainPath, String id) {
        given()
                .when()
                .delete(domainPath + "/{id}", id)
                .then()
                .statusCode(204);
        createdResources.remove(new TestResource(domainPath, id));
    }

    @AfterEach
    void cleanupCreatedResources() {
        for (int index = createdResources.size() - 1; index >= 0; index--) {
            TestResource resource = createdResources.get(index);
            given().when().delete(resource.domainPath() + "/{id}", resource.id());
        }
        createdResources.clear();
    }

    protected void assertCountEndpoint(String domainPath, String... matchingKeywords) {
        Number total = given()
                .when()
                .get(domainPath + "/count")
                .then()
                .statusCode(200)
                .body("$", aMapWithSize(1))
                .extract()
                .path("count");

        for (String blankKeyword : List.of("", "   ")) {
            given()
                    .queryParam("keyword", blankKeyword)
                    .when()
                    .get(domainPath + "/count")
                    .then()
                    .statusCode(200)
                    .body("$", aMapWithSize(1))
                    .body("count", equalTo(total));
        }

        for (String matchingKeyword : matchingKeywords) {
            given()
                    .queryParam("keyword", matchingKeyword)
                    .when()
                    .get(domainPath + "/count")
                    .then()
                    .statusCode(200)
                    .body("$", aMapWithSize(1))
                    .body("count", greaterThanOrEqualTo(1));
        }

        given()
                .queryParam("keyword", "no-match-" + randomSuffix())
                .when()
                .get(domainPath + "/count")
                .then()
                .statusCode(200)
                .body("$", aMapWithSize(1))
                .body("count", equalTo(0));
    }
}
