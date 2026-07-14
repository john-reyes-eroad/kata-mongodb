package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiDiagnosticEventsEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void diagnosticEventCrudShouldWork() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vehicleId", vehicleId);
        payload.put("code", "P0001-" + suffix);
        payload.put("severity", "HIGH");
        payload.put("description", "Fuel Volume Regulator issue " + suffix);
        payload.put("occurredAt", Instant.now().toString());

        String eventId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/diagnostic-events")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given().when().get("/api/diagnostic-events").then().statusCode(200).body("id", hasItem(eventId));
        given().when().get("/api/diagnostic-events/{id}", eventId).then().statusCode(200).body("id", equalTo(eventId));

        Map<String, Object> updatePayload = new LinkedHashMap<>(payload);
        updatePayload.put("severity", "MEDIUM");

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/diagnostic-events/{id}", eventId)
                .then()
                .statusCode(200)
                .body("id", equalTo(eventId))
                .body("severity", equalTo("MEDIUM"));

        given().when().delete("/api/diagnostic-events/{id}", eventId).then().statusCode(204);
        given().when().get("/api/diagnostic-events/{id}", eventId).then().statusCode(404).body("code", equalTo("not_found"));
        given().when().delete("/api/vehicles/{id}", vehicleId).then().statusCode(204);
    }
}

