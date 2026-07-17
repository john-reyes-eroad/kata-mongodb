package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class KongTripsEndpointBlackboxTest extends AbstractKongBlackboxTest {

    @Test
    void tripCrudShouldWork() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        String driverId = createDriver(suffix);
        Map<String, Object> payload = tripPayload(vehicleId, driverId);

        String tripId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("vehicle.id", equalTo(vehicleId))
                .body("driver.id", equalTo(driverId))
                .body("distanceKm", equalTo(((BigDecimal) payload.get("distanceKm")).floatValue()))
                .extract()
                .path("id");
        trackResource("/api/trips", tripId);

        given().when().get("/api/trips").then().statusCode(200).body("id", hasItem(tripId));
        given()
                .when()
                .get("/api/trips/{id}", tripId)
                .then()
                .statusCode(200)
                .body("id", equalTo(tripId))
                .body("vehicle.id", equalTo(vehicleId))
                .body("driver.id", equalTo(driverId))
                .body("distanceKm", equalTo(((BigDecimal) payload.get("distanceKm")).floatValue()));

        Map<String, Object> updatePayload = tripPayload(vehicleId, driverId);
        updatePayload.put("distanceKm", new BigDecimal("155.2"));
        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/trips/{id}", tripId)
                .then()
                .statusCode(200)
                .body("id", equalTo(tripId))
                .body("vehicle.id", equalTo(vehicleId))
                .body("driver.id", equalTo(driverId))
                .body("distanceKm", equalTo(((BigDecimal) updatePayload.get("distanceKm")).floatValue()));
        given()
                .when()
                .get("/api/trips/{id}", tripId)
                .then()
                .statusCode(200)
                .body("distanceKm", equalTo(((BigDecimal) updatePayload.get("distanceKm")).floatValue()));

        deleteResource("/api/trips", tripId);
        given().when().get("/api/trips/{id}", tripId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void updatingUnknownTripShouldReturnNotFound() {
        TripFixture fixture = createTripFixture(randomSuffix());

        given()
                .contentType(ContentType.JSON)
                .body(tripPayload(fixture.vehicleId(), fixture.driverId()))
                .when()
                .put("/api/trips/{id}", "missing-" + randomSuffix())
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    void tripCountShouldSupportAbsentBlankMatchingAndNonmatchingKeywords() {
        TripFixture fixture = createTripFixture(randomSuffix());

        assertCountEndpoint(
                "/api/trips",
                fixture.tripId(),
                fixture.vehicleId(),
                fixture.driverId()
        );
    }
}
