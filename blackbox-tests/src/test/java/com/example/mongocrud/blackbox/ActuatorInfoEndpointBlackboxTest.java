package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

class ActuatorInfoEndpointBlackboxTest extends AbstractBlackboxTest {

    @Test
    void infoShouldReturn200() {
        given()
                .when()
                .get("/actuator/info")
                .then()
                .statusCode(200);
    }
}

