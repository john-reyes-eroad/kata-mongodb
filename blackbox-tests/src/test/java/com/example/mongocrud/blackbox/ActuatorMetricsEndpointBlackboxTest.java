package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasKey;

import org.junit.jupiter.api.Test;

class ActuatorMetricsEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void metricsShouldReturn200() {
        given()
                .when()
                .get("/actuator/metrics")
                .then()
                .statusCode(200)
                .body("$", hasKey("names"));
    }
}

