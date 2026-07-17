package com.example.mongocrud.blackbox;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.jupiter.api.BeforeAll;

abstract class AbstractKongBlackboxTest extends AbstractBlackboxTest {

    @BeforeAll
    static void configureBaseUrl() {
        String fromProperty = System.getProperty("kong.baseUrl");
        String fromEnv = System.getenv("KONG_BASE_URL");
        RestAssured.baseURI = fromProperty != null && !fromProperty.isBlank()
                ? fromProperty
                : (fromEnv != null && !fromEnv.isBlank() ? fromEnv : "http://localhost:9090");
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .addHeader("Authorization", "Bearer test-token")
                .build();
    }
}
