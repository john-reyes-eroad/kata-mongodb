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

class ApiLocationsEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void locationCrudShouldWork() {
        String suffix = randomSuffix();
        TripFixture fixture = createTripFixture(suffix);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("tripId", fixture.tripId());
        payload.put("latitude", new BigDecimal("-36.8485"));
        payload.put("longitude", new BigDecimal("174.7633"));
        payload.put("recordedAt", Instant.now().toString());

        String locationId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/locations")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given().when().get("/api/locations").then().statusCode(200).body("id", hasItem(locationId));
        given().when().get("/api/locations/{id}", locationId).then().statusCode(200).body("id", equalTo(locationId));

        Map<String, Object> updatePayload = new LinkedHashMap<>(payload);
        updatePayload.put("latitude", new BigDecimal("-36.8500"));

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/locations/{id}", locationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(locationId));

        given().when().delete("/api/locations/{id}", locationId).then().statusCode(204);
        given().when().get("/api/locations/{id}", locationId).then().statusCode(404).body("code", equalTo("not_found"));
        given().when().delete("/api/trips/{id}", fixture.tripId()).then().statusCode(204);
        given().when().delete("/api/drivers/{id}", fixture.driverId()).then().statusCode(204);
        given().when().delete("/api/vehicles/{id}", fixture.vehicleId()).then().statusCode(204);
    }
}

