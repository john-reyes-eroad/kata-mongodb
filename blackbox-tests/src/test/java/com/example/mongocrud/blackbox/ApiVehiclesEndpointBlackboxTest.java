package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiVehiclesEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void vehicleCrudAndSearchShouldWork() {
        String suffix = randomSuffix();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vin", "VIN-" + suffix);
        payload.put("make", "COVESA");
        payload.put("model", "Model-" + suffix);
        payload.put("year", 2024);

        String vehicleId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given().when().get("/api/vehicles").then().statusCode(200).body("id", hasItem(vehicleId));
        given().queryParam("keyword", suffix).when().get("/api/vehicles/search").then().statusCode(200).body("id", hasItem(vehicleId));
        given().when().get("/api/vehicles/{id}", vehicleId).then().statusCode(200).body("id", equalTo(vehicleId));

        Map<String, Object> updatePayload = new LinkedHashMap<>();
        updatePayload.put("vin", "VIN-" + suffix + "-U");
        updatePayload.put("make", "COVESA");
        updatePayload.put("model", "Model-Updated-" + suffix);
        updatePayload.put("year", 2025);

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/vehicles/{id}", vehicleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(vehicleId))
                .body("model", equalTo(updatePayload.get("model")));

        given().when().delete("/api/vehicles/{id}", vehicleId).then().statusCode(204);
        given().when().get("/api/vehicles/{id}", vehicleId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void creatingVehicleWithDuplicateVinShouldReturn409() {
        String suffix = randomSuffix();
        String vin = "VIN-UNIQUE-" + suffix;

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("vin", vin);
        payload.put("make", "COVESA");
        payload.put("model", "Duplicate-Check-" + suffix);
        payload.put("year", 2024);

        String vehicleId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));

        given().when().delete("/api/vehicles/{id}", vehicleId).then().statusCode(204);
    }
}
