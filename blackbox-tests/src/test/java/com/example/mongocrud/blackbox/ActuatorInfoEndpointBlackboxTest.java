package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.instanceOf;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ActuatorInfoEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void infoShouldReturn200() {
        given()
                .when()
                .get("/actuator/info")
                .then()
                .statusCode(200)
                .body("$", instanceOf(Map.class));
    }
}
