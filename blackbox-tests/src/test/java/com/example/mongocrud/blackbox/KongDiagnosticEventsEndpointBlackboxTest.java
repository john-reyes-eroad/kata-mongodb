package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class KongDiagnosticEventsEndpointBlackboxTest extends AbstractKongBlackboxTest {

    @Test
    void diagnosticEventCrudShouldWork() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        Map<String, Object> payload = diagnosticEventPayload(vehicleId, suffix);

        String eventId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/diagnostic-events")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("vehicle.id", equalTo(vehicleId))
                .body("code", equalTo(payload.get("code")))
                .body("severity", equalTo(payload.get("severity")))
                .body("description", equalTo(payload.get("description")))
                .body("occurredAt", notNullValue())
                .extract()
                .path("id");
        trackResource("/api/diagnostic-events", eventId);

        given().when().get("/api/diagnostic-events").then().statusCode(200).body("id", hasItem(eventId));
        given()
                .when()
                .get("/api/diagnostic-events/{id}", eventId)
                .then()
                .statusCode(200)
                .body("id", equalTo(eventId))
                .body("vehicle.id", equalTo(vehicleId))
                .body("code", equalTo(payload.get("code")))
                .body("severity", equalTo(payload.get("severity")))
                .body("description", equalTo(payload.get("description")));

        Map<String, Object> updatePayload = new LinkedHashMap<>(payload);
        updatePayload.put("severity", "MEDIUM-" + suffix);
        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/diagnostic-events/{id}", eventId)
                .then()
                .statusCode(200)
                .body("id", equalTo(eventId))
                .body("vehicle.id", equalTo(vehicleId))
                .body("severity", equalTo(updatePayload.get("severity")));
        given()
                .when()
                .get("/api/diagnostic-events/{id}", eventId)
                .then()
                .statusCode(200)
                .body("severity", equalTo(updatePayload.get("severity")));

        deleteResource("/api/diagnostic-events", eventId);
        given().when().get("/api/diagnostic-events/{id}", eventId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void updatingUnknownDiagnosticEventShouldReturnNotFound() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);

        given()
                .contentType(ContentType.JSON)
                .body(diagnosticEventPayload(vehicleId, suffix))
                .when()
                .put("/api/diagnostic-events/{id}", "missing-" + randomSuffix())
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    void diagnosticEventCountShouldSupportAbsentBlankMatchingAndNonmatchingKeywords() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        Map<String, Object> payload = diagnosticEventPayload(vehicleId, suffix);
        String eventId = createDiagnosticEvent(payload);

        assertCountEndpoint(
                "/api/diagnostic-events",
                (String) payload.get("code"),
                (String) payload.get("severity"),
                (String) payload.get("description"),
                eventId,
                vehicleId
        );
    }
}
