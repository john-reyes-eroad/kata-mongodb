package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;

import java.util.Map;

import org.junit.jupiter.api.Test;

class KongResponseContractBlackboxTest extends AbstractKongBlackboxTest {

    @Test
    void everyDomainShouldExposeItsCompleteResponseContract() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        String driverId = createDriver(suffix);
        String tripId = createTrip(vehicleId, driverId);
        String locationId = createLocation(locationPayload(tripId));
        String eventId = createDiagnosticEvent(diagnosticEventPayload(vehicleId, suffix));

        assertVehicleContract(given().when().get("/api/vehicles/{id}", vehicleId).then());
        assertDriverContract(given().when().get("/api/drivers/{id}", driverId).then());
        assertTripContract(given().when().get("/api/trips/{id}", tripId).then());
        assertLocationContract(given().when().get("/api/locations/{id}", locationId).then());
        assertDiagnosticEventContract(given().when().get("/api/diagnostic-events/{id}", eventId).then());
    }
}
