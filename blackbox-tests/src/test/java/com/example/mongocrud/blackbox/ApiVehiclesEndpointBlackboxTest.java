package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiVehiclesEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void vehicleCrudAndSearchShouldWork() {
        String suffix = randomSuffix();
        Map<String, Object> payload = vehiclePayload(suffix);

        String vehicleId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("vin", equalTo(payload.get("vin")))
                .body("make", equalTo(payload.get("make")))
                .body("model", equalTo(payload.get("model")))
                .body("year", equalTo(payload.get("year")))
                .extract()
                .path("id");
        trackResource("/api/vehicles", vehicleId);

        given().when().get("/api/vehicles").then().statusCode(200).body("id", hasItem(vehicleId));
        assertVehicleSearchFinds("/api/vehicles/search", payload.get("vin"), vehicleId);
        assertVehicleSearchFinds("/api/vehicles/search", payload.get("make"), vehicleId);
        assertVehicleSearchFinds("/api/vehicles/search", payload.get("model"), vehicleId);
        given()
                .queryParam("keyword", "no-match-" + randomSuffix())
                .when()
                .get("/api/vehicles/search")
                .then()
                .statusCode(200)
                .body("$", empty());
        given()
                .when()
                .get("/api/vehicles/{id}", vehicleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(vehicleId))
                .body("vin", equalTo(payload.get("vin")))
                .body("make", equalTo(payload.get("make")))
                .body("model", equalTo(payload.get("model")))
                .body("year", equalTo(payload.get("year")));

        Map<String, Object> updatePayload = vehiclePayload(suffix + "-updated");
        updatePayload.put("year", 2025);

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/vehicles/{id}", vehicleId)
                .then()
                .statusCode(200)
                .body("id", equalTo(vehicleId))
                .body("vin", equalTo(updatePayload.get("vin")))
                .body("make", equalTo(updatePayload.get("make")))
                .body("model", equalTo(updatePayload.get("model")))
                .body("year", equalTo(updatePayload.get("year")));
        given()
                .when()
                .get("/api/vehicles/{id}", vehicleId)
                .then()
                .statusCode(200)
                .body("model", equalTo(updatePayload.get("model")));

        deleteResource("/api/vehicles", vehicleId);
        given().when().get("/api/vehicles/{id}", vehicleId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void creatingVehicleWithDuplicateVinShouldReturnConflict() {
        Map<String, Object> payload = vehiclePayload(randomSuffix());
        createVehicle(payload);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/vehicles")
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));
    }

    @Test
    void updatingVehicleWithDuplicateVinShouldReturnConflictWithoutChangingIt() {
        String suffix = randomSuffix();
        Map<String, Object> firstVehicle = vehiclePayload(suffix + "-first");
        Map<String, Object> secondVehicle = vehiclePayload(suffix + "-second");
        createVehicle(firstVehicle);
        String secondVehicleId = createVehicle(secondVehicle);

        given()
                .contentType(ContentType.JSON)
                .body(firstVehicle)
                .when()
                .put("/api/vehicles/{id}", secondVehicleId)
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));
        given()
                .when()
                .get("/api/vehicles/{id}", secondVehicleId)
                .then()
                .statusCode(200)
                .body("vin", equalTo(secondVehicle.get("vin")));
    }

    @Test
    void updatingUnknownVehicleShouldReturnNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body(vehiclePayload(randomSuffix()))
                .when()
                .put("/api/vehicles/{id}", "missing-" + randomSuffix())
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    void searchingVehiclesWithoutKeywordShouldReturnValidationError() {
        given()
                .when()
                .get("/api/vehicles/search")
                .then()
                .statusCode(400)
                .body("code", equalTo("validation_error"))
                .body("fields[0].field", equalTo("keyword"))
                .body("fields[0].message", equalTo("Parameter is required"));
    }

    @Test
    void vehicleCountShouldSupportAbsentBlankMatchingAndNonmatchingKeywords() {
        Map<String, Object> payload = vehiclePayload(randomSuffix());
        createVehicle(payload);

        assertCountEndpoint(
                "/api/vehicles",
                (String) payload.get("vin"),
                (String) payload.get("make"),
                (String) payload.get("model")
        );
    }

    private void assertVehicleSearchFinds(String path, Object keyword, String vehicleId) {
        given()
                .queryParam("keyword", keyword)
                .when()
                .get(path)
                .then()
                .statusCode(200)
                .body("id", hasItem(vehicleId));
    }
}
