package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class KongAuthorizationBlackboxTest extends AbstractKongBlackboxTest {

    private static final String[] PROTECTED_PATHS = {
        "/api/vehicles",
        "/api/drivers",
        "/api/trips",
        "/api/locations",
        "/api/diagnostic-events"
    };

    @BeforeAll
    static void configureBaseUrl() {
        var fromProperty = System.getProperty("kong.baseUrl");
        var fromEnv = System.getenv("KONG_BASE_URL");
        RestAssured.baseURI = fromProperty != null && !fromProperty.isBlank()
                ? fromProperty
                : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:9090");
        RestAssured.requestSpecification = null;
    }

    @Test
    void requestsWithoutAuthorizationHeaderShouldReturn403() {
        for (String path : PROTECTED_PATHS) {
            given()
                    .when()
                    .get(path)
                    .then()
                    .statusCode(403)
                    .body("message", equalTo("Forbidden"));
        }
    }

    @Test
    void requestsWithInvalidTokenShouldReturn403() {
        for (String path : PROTECTED_PATHS) {
            given()
                    .header("Authorization", "invalid-token")
                    .when()
                    .get(path)
                    .then()
                    .statusCode(403)
                    .body("message", equalTo("Forbidden"));
        }
    }

    @Test
    void requestsWithValidBearerTokenShouldBeAllowed() {
        for (String path : PROTECTED_PATHS) {
            given()
                    .header("Authorization", "Bearer any-value")
                    .when()
                    .get(path)
                    .then()
                    .statusCode(200);
        }
    }
}
