package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiDriversEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void driverCrudShouldWork() {
        String suffix = randomSuffix();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", "Driver " + suffix);
        payload.put("licenseNumber", "LIC-" + suffix);

        String driverId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .extract()
                .path("id");

        given().when().get("/api/drivers").then().statusCode(200).body("id", hasItem(driverId));
        given().queryParam("keyword", suffix).when().get("/api/drivers/search").then().statusCode(200).body("id", hasItem(driverId));
        given().when().get("/api/drivers/{id}", driverId).then().statusCode(200).body("id", equalTo(driverId));

        Map<String, Object> updatePayload = new LinkedHashMap<>();
        updatePayload.put("name", "Updated Driver " + suffix);
        updatePayload.put("licenseNumber", "LIC-U-" + suffix);

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/drivers/{id}", driverId)
                .then()
                .statusCode(200)
                .body("name", equalTo(updatePayload.get("name")));

        given().when().delete("/api/drivers/{id}", driverId).then().statusCode(204);
        given().when().get("/api/drivers/{id}", driverId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void creatingDuplicateDriverNameOrLicenseShouldReturnConflict() {
        String suffix = randomSuffix();

        Map<String, Object> firstDriver = new LinkedHashMap<>();
        firstDriver.put("name", "Unique Name " + suffix);
        firstDriver.put("licenseNumber", "LIC-UNIQUE-" + suffix);

        given()
                .contentType(ContentType.JSON)
                .body(firstDriver)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201);

        Map<String, Object> duplicateName = new LinkedHashMap<>();
        duplicateName.put("name", firstDriver.get("name"));
        duplicateName.put("licenseNumber", "LIC-OTHER-" + suffix);

        given()
                .contentType(ContentType.JSON)
                .body(duplicateName)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));

        Map<String, Object> duplicateLicense = new LinkedHashMap<>();
        duplicateLicense.put("name", "Other Name " + suffix);
        duplicateLicense.put("licenseNumber", firstDriver.get("licenseNumber"));

        given()
                .contentType(ContentType.JSON)
                .body(duplicateLicense)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));
    }
}
