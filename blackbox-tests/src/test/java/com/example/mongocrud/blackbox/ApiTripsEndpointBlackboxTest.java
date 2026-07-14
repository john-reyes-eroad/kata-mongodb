package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiTripsEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void tripCrudShouldWork() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        String driverId = createDriver(suffix);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("driverId", driverId);
        payload.put("startTime", Instant.now().minusSeconds(7200).toString());
        payload.put("endTime", Instant.now().minusSeconds(3600).toString());
        payload.put("distanceKm", new BigDecimal("120.7"));

        String tripId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given().when().get("/api/trips").then().statusCode(200).body("id", hasItem(tripId));
        given().when().get("/api/trips/{id}", tripId).then().statusCode(200).body("id", equalTo(tripId));

        Map<String, Object> updatePayload = new LinkedHashMap<>();
        updatePayload.put("vehicleId", vehicleId);
        updatePayload.put("driverId", driverId);
        updatePayload.put("startTime", Instant.now().minusSeconds(3600).toString());
        updatePayload.put("endTime", Instant.now().toString());
        updatePayload.put("distanceKm", new BigDecimal("155.2"));

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/trips/{id}", tripId)
                .then()
                .statusCode(200)
                .body("id", equalTo(tripId));

        given().when().delete("/api/trips/{id}", tripId).then().statusCode(204);
        given().when().get("/api/trips/{id}", tripId).then().statusCode(404).body("code", equalTo("not_found"));
        given().when().delete("/api/drivers/{id}", driverId).then().statusCode(204);
        given().when().delete("/api/vehicles/{id}", vehicleId).then().statusCode(204);
    }
}

