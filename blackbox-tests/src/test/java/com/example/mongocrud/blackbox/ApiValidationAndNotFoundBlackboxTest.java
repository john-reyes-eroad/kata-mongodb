package com.example.mongocrud.blackbox;

import static io.restassured.RestAssured.given;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

class ApiValidationAndNotFoundBlackboxTest extends AbstractBlackboxTest {

    private static final String NONEXISTENT_OBJECT_ID = "000000000000000000000000";
    private static final String MALFORMED_OBJECT_ID = "not-an-object-id";

    @Test
    void vehicleRequestConstraintsShouldReturnCanonicalValidationErrors() {
        Map<String, Object> payload = vehiclePayload(randomSuffix());
        assertInvalidVehicle(payload, "vin", "");
        assertInvalidVehicle(payload, "make", " ");
        assertInvalidVehicle(payload, "model", null);
        assertInvalidVehicle(payload, "year", 1899);
        assertInvalidVehicle(payload, "year", 2101);
    }

    @Test
    void driverRequestConstraintsShouldReturnCanonicalValidationErrors() {
        Map<String, Object> payload = driverPayload(randomSuffix());
        assertInvalidDriver(payload, "name", "");
        assertInvalidDriver(payload, "licenseNumber", " ");
    }

    @Test
    void tripRequestConstraintsShouldReturnCanonicalValidationErrors() {
        TripFixture fixture = createTripFixture(randomSuffix());
        Map<String, Object> payload = tripPayload(fixture.vehicleId(), fixture.driverId());
        assertInvalid("/api/trips", payload, "vehicleId", "");
        assertInvalid("/api/trips", payload, "driverId", " ");
        assertInvalid("/api/trips", payload, "startTime", null);
        assertInvalid("/api/trips", payload, "distanceKm", null);
        assertInvalid("/api/trips", payload, "distanceKm", new BigDecimal("-0.1"));
    }

    @Test
    void locationRequestConstraintsShouldReturnCanonicalValidationErrors() {
        TripFixture fixture = createTripFixture(randomSuffix());
        Map<String, Object> payload = locationPayload(fixture.tripId());
        assertInvalid("/api/locations", payload, "tripId", "");
        assertInvalid("/api/locations", payload, "latitude", null);
        assertInvalid("/api/locations", payload, "latitude", new BigDecimal("-90.1"));
        assertInvalid("/api/locations", payload, "latitude", new BigDecimal("90.1"));
        assertInvalid("/api/locations", payload, "longitude", null);
        assertInvalid("/api/locations", payload, "longitude", new BigDecimal("-180.1"));
        assertInvalid("/api/locations", payload, "longitude", new BigDecimal("180.1"));
        assertInvalid("/api/locations", payload, "recordedAt", null);
    }

    @Test
    void diagnosticEventRequestConstraintsShouldReturnCanonicalValidationErrors() {
        String suffix = randomSuffix();
        String vehicleId = createVehicle(suffix);
        Map<String, Object> payload = diagnosticEventPayload(vehicleId, suffix);
        assertInvalid("/api/diagnostic-events", payload, "vehicleId", "");
        assertInvalid("/api/diagnostic-events", payload, "code", " ");
        assertInvalid("/api/diagnostic-events", payload, "severity", null);
        assertInvalid("/api/diagnostic-events", payload, "description", "");
        assertInvalid("/api/diagnostic-events", payload, "occurredAt", null);
    }

    @Test
    void everyIdRouteShouldReturnCanonicalNotFoundForMalformedAndUnknownObjectIds() {
        for (String id : List.of(MALFORMED_OBJECT_ID, NONEXISTENT_OBJECT_ID)) {
            assertVehicleIdRoutesNotFound(id);
            assertDriverIdRoutesNotFound(id);
            assertTripIdRoutesNotFound(id);
            assertLocationIdRoutesNotFound(id);
            assertDiagnosticEventIdRoutesNotFound(id);
        }
    }

    @Test
    void relatedResourceIdsShouldReturnCanonicalNotFoundOnCreateAndUpdate() {
        TripFixture fixture = createTripFixture(randomSuffix());
        String locationId = createLocation(locationPayload(fixture.tripId()));
        String eventId = createDiagnosticEvent(diagnosticEventPayload(fixture.vehicleId(), randomSuffix()));

        for (String invalidId : List.of(MALFORMED_OBJECT_ID, NONEXISTENT_OBJECT_ID)) {
            assertNotFound(post("/api/trips", with(tripPayload(fixture.vehicleId(), fixture.driverId()), "vehicleId", invalidId)));
            assertNotFound(post("/api/trips", with(tripPayload(fixture.vehicleId(), fixture.driverId()), "driverId", invalidId)));
            assertNotFound(put("/api/trips/" + fixture.tripId(), with(tripPayload(fixture.vehicleId(), fixture.driverId()), "vehicleId", invalidId)));
            assertNotFound(put("/api/trips/" + fixture.tripId(), with(tripPayload(fixture.vehicleId(), fixture.driverId()), "driverId", invalidId)));

            assertNotFound(post("/api/locations", with(locationPayload(fixture.tripId()), "tripId", invalidId)));
            assertNotFound(put("/api/locations/" + locationId, with(locationPayload(fixture.tripId()), "tripId", invalidId)));

            assertNotFound(post("/api/diagnostic-events",
                    with(diagnosticEventPayload(fixture.vehicleId(), randomSuffix()), "vehicleId", invalidId)));
            assertNotFound(put("/api/diagnostic-events/" + eventId,
                    with(diagnosticEventPayload(fixture.vehicleId(), randomSuffix()), "vehicleId", invalidId)));
        }
    }

    @Test
    void blankVehicleAndDriverSearchesShouldReturnAllRecords() {
        String vehicleId = createVehicle(randomSuffix());
        String driverId = createDriver(randomSuffix());

        for (String keyword : List.of("", "   ")) {
            given().queryParam("keyword", keyword).when().get("/api/vehicles/search").then()
                    .statusCode(200).body("id", org.hamcrest.Matchers.hasItem(vehicleId));
            given().queryParam("keyword", keyword).when().get("/api/drivers/search").then()
                    .statusCode(200).body("id", org.hamcrest.Matchers.hasItem(driverId));
        }
    }

    private void assertVehicleIdRoutesNotFound(String id) {
        assertNotFound(given().when().get("/api/vehicles/{id}", id).then());
        assertNotFound(put("/api/vehicles/" + id, vehiclePayload(randomSuffix())));
        assertNotFound(given().when().delete("/api/vehicles/{id}", id).then());
    }

    private void assertDriverIdRoutesNotFound(String id) {
        assertNotFound(given().when().get("/api/drivers/{id}", id).then());
        assertNotFound(put("/api/drivers/" + id, driverPayload(randomSuffix())));
        assertNotFound(given().when().delete("/api/drivers/{id}", id).then());
    }

    private void assertTripIdRoutesNotFound(String id) {
        TripFixture fixture = createTripFixture(randomSuffix());
        assertNotFound(given().when().get("/api/trips/{id}", id).then());
        assertNotFound(put("/api/trips/" + id, tripPayload(fixture.vehicleId(), fixture.driverId())));
        assertNotFound(given().when().delete("/api/trips/{id}", id).then());
    }

    private void assertLocationIdRoutesNotFound(String id) {
        TripFixture fixture = createTripFixture(randomSuffix());
        assertNotFound(given().when().get("/api/locations/{id}", id).then());
        assertNotFound(put("/api/locations/" + id, locationPayload(fixture.tripId())));
        assertNotFound(given().when().delete("/api/locations/{id}", id).then());
    }

    private void assertDiagnosticEventIdRoutesNotFound(String id) {
        String vehicleId = createVehicle(randomSuffix());
        assertNotFound(given().when().get("/api/diagnostic-events/{id}", id).then());
        assertNotFound(put("/api/diagnostic-events/" + id, diagnosticEventPayload(vehicleId, randomSuffix())));
        assertNotFound(given().when().delete("/api/diagnostic-events/{id}", id).then());
    }

    private void assertInvalidVehicle(Map<String, Object> payload, String field, Object value) {
        assertInvalid("/api/vehicles", payload, field, value);
    }

    private void assertInvalidDriver(Map<String, Object> payload, String field, Object value) {
        assertInvalid("/api/drivers", payload, field, value);
    }

    private void assertInvalid(String path, Map<String, Object> payload, String field, Object value) {
        assertValidationError(post(path, with(payload, field, value)), field);
    }

    private Map<String, Object> with(Map<String, Object> payload, String field, Object value) {
        Map<String, Object> copy = new LinkedHashMap<>(payload);
        copy.put(field, value);
        return copy;
    }

    private ValidatableResponse post(String path, Map<String, Object> payload) {
        return given().contentType(ContentType.JSON).body(payload).when().post(path).then();
    }

    private ValidatableResponse put(String path, Map<String, Object> payload) {
        return given().contentType(ContentType.JSON).body(payload).when().put(path).then();
    }
}
