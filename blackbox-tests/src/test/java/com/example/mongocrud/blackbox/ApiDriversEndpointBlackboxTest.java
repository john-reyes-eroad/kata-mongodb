package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;

import java.util.LinkedHashMap;
import java.util.Map;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

class ApiDriversEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void driverCrudAndSearchShouldWork() {
        String suffix = randomSuffix();
        Map<String, Object> payload = driverPayload(suffix);

        String driverId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo(payload.get("name")))
                .body("licenseNumber", equalTo(payload.get("licenseNumber")))
                .extract()
                .path("id");
        trackResource("/api/drivers", driverId);

        given().when().get("/api/drivers").then().statusCode(200).body("id", hasItem(driverId));
        assertDriverSearchFinds(payload.get("name"), driverId);
        assertDriverSearchFinds(payload.get("licenseNumber"), driverId);
        given()
                .queryParam("keyword", "no-match-" + randomSuffix())
                .when()
                .get("/api/drivers/search")
                .then()
                .statusCode(200)
                .body("$", empty());
        given()
                .when()
                .get("/api/drivers/{id}", driverId)
                .then()
                .statusCode(200)
                .body("id", equalTo(driverId))
                .body("name", equalTo(payload.get("name")))
                .body("licenseNumber", equalTo(payload.get("licenseNumber")));

        Map<String, Object> updatePayload = driverPayload(suffix + "-updated");

        given()
                .contentType(ContentType.JSON)
                .body(updatePayload)
                .when()
                .put("/api/drivers/{id}", driverId)
                .then()
                .statusCode(200)
                .body("id", equalTo(driverId))
                .body("name", equalTo(updatePayload.get("name")))
                .body("licenseNumber", equalTo(updatePayload.get("licenseNumber")));
        given()
                .when()
                .get("/api/drivers/{id}", driverId)
                .then()
                .statusCode(200)
                .body("name", equalTo(updatePayload.get("name")));

        deleteResource("/api/drivers", driverId);
        given().when().get("/api/drivers/{id}", driverId).then().statusCode(404).body("code", equalTo("not_found"));
    }

    @Test
    void creatingDuplicateDriverNameOrLicenseShouldReturnConflict() {
        Map<String, Object> firstDriver = driverPayload(randomSuffix());
        createDriver(firstDriver);

        Map<String, Object> duplicateName = new LinkedHashMap<>(driverPayload(randomSuffix()));
        duplicateName.put("name", firstDriver.get("name"));
        given()
                .contentType(ContentType.JSON)
                .body(duplicateName)
                .when()
                .post("/api/drivers")
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));

        Map<String, Object> duplicateLicense = new LinkedHashMap<>(driverPayload(randomSuffix()));
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

    @Test
    void updatingDriverWithDuplicateNameOrLicenseShouldReturnConflictWithoutChangingIt() {
        String suffix = randomSuffix();
        Map<String, Object> firstDriver = driverPayload(suffix + "-first");
        Map<String, Object> secondDriver = driverPayload(suffix + "-second");
        createDriver(firstDriver);
        String secondDriverId = createDriver(secondDriver);

        Map<String, Object> duplicateName = new LinkedHashMap<>(secondDriver);
        duplicateName.put("name", firstDriver.get("name"));
        given()
                .contentType(ContentType.JSON)
                .body(duplicateName)
                .when()
                .put("/api/drivers/{id}", secondDriverId)
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));

        Map<String, Object> duplicateLicense = new LinkedHashMap<>(secondDriver);
        duplicateLicense.put("licenseNumber", firstDriver.get("licenseNumber"));
        given()
                .contentType(ContentType.JSON)
                .body(duplicateLicense)
                .when()
                .put("/api/drivers/{id}", secondDriverId)
                .then()
                .statusCode(409)
                .body("code", equalTo("conflict"));

        given()
                .when()
                .get("/api/drivers/{id}", secondDriverId)
                .then()
                .statusCode(200)
                .body("name", equalTo(secondDriver.get("name")))
                .body("licenseNumber", equalTo(secondDriver.get("licenseNumber")));
    }

    @Test
    void updatingUnknownDriverShouldReturnNotFound() {
        given()
                .contentType(ContentType.JSON)
                .body(driverPayload(randomSuffix()))
                .when()
                .put("/api/drivers/{id}", "missing-" + randomSuffix())
                .then()
                .statusCode(404)
                .body("code", equalTo("not_found"));
    }

    @Test
    void searchingDriversWithoutKeywordShouldReturnValidationError() {
        given()
                .when()
                .get("/api/drivers/search")
                .then()
                .statusCode(400)
                .body("code", equalTo("validation_error"))
                .body("fields[0].field", equalTo("keyword"))
                .body("fields[0].message", equalTo("Parameter is required"));
    }

    @Test
    void driverCountShouldSupportAbsentBlankMatchingAndNonmatchingKeywords() {
        Map<String, Object> payload = driverPayload(randomSuffix());
        createDriver(payload);

        assertCountEndpoint(
                "/api/drivers",
                (String) payload.get("name"),
                (String) payload.get("licenseNumber")
        );
    }

    private void assertDriverSearchFinds(Object keyword, String driverId) {
        given()
                .queryParam("keyword", keyword)
                .when()
                .get("/api/drivers/search")
                .then()
                .statusCode(200)
                .body("id", hasItem(driverId));
    }
}
