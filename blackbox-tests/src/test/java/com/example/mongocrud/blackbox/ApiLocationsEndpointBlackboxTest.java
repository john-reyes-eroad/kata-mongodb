package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiLocationsEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void locationCrudShouldWork() {
        TripFixture fixture = createTripFixture(randomSuffix());
        Map<String, Object> payload = locationPayload(fixture.tripId());

        String locationId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/locations")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("trip.id", equalTo(fixture.tripId()))
                .body("recordedAt", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/locations", locationId);

        given().when().get("/api/locations").then().statusCode(200).body("id", hasItem(locationId));
        given()
                .when()
                .get("/api/locations/{id}", locationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(locationId))
                .body("trip.id", equalTo(fixture.tripId()))
                .body("recordedAt", notNullValue());

        Map<String, Object> updatePayload = locationPayload(fixture.tripId());
        updatePayload.put("latitude", new BigDecimal("-36.8500"));
        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/locations/{id}", locationId)
                .then()
                .statusCode(200)
                .body("id", equalTo(locationId))
                .body("trip.id", equalTo(fixture.tripId()))
                .body("recordedAt", notNullValue());
        given()
                .when()
                .get("/api/locations/{id}", locationId)
                .then()
                .statusCode(200)
                .body("trip.id", equalTo(fixture.tripId()));

        deleteResource("/api/locations", locationId);
        given().when().get("/api/locations/{id}", locationId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void updatingUnknownLocationShouldReturnNotFound() {
        TripFixture fixture = createTripFixture(randomSuffix());

        given()
                .contentType(ContentType.JSON)
                .body(locationPayload(fixture.tripId()))
                .when()
                .put("/api/locations/{id}", "missing-" + randomSuffix())
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    void locationCountShouldSupportAbsentBlankMatchingAndNonmatchingKeywords() {
        TripFixture fixture = createTripFixture(randomSuffix());
        String locationId = createLocation(locationPayload(fixture.tripId()));

        assertCountEndpoint(
                "/api/locations",
                locationId,
                fixture.tripId()
        );
    }
}
