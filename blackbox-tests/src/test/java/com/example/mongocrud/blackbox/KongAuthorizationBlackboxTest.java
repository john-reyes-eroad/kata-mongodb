package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.Test;

class KongAuthorizationBlackboxTest extends AbstractKongBlackboxTest {

    private static final String[] PROTECTED_PATHS = {
        "/api/vehicles",
        "/api/drivers",
        "/api/trips",
        "/api/locations",
        "/api/diagnostic-events"
    };

    @Test
    void requestsWithoutAuthorizationHeaderShouldReturn403() {
        for (String path : PROTECTED_PATHS) {
            given(new RequestSpecBuilder().build())
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
            given(new RequestSpecBuilder().addHeader("Authorization", "invalid-token").build())
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
            given(new RequestSpecBuilder().addHeader("Authorization", "Bearer any-value").build())
                    .when()
                    .get(path)
                    .then()
                    .statusCode(200);
        }
    }
}
