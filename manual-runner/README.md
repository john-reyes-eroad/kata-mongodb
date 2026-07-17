# manual-runner

A standalone Spring Boot app for manually exercising domain repository implementations against a live MongoDB instance. No web server, no REST layer - just direct calls to the outbound adapter.

## Purpose

Use this module when you want to:
- Manually test a repository method end-to-end against a real database
- Explore MongoDB behaviour (indexing, duplicate key errors, queries, etc.)
- Iterate quickly on a new repository feature without spinning up the full app

## Prerequisites

MongoDB running locally on `localhost:27017`. Start it with:

```bash
docker-compose up mongo
```

The `app-java` and `app-native` services use Docker Compose profiles so they are excluded by default.

## Running

Each runner is gated behind a Spring profile matching its domain name. Activate exactly one profile to run that runner in isolation.

### From the terminal

```bash
mvn spring-boot:run -pl manual-runner -Dspring-boot.run.profiles=vehicle
mvn spring-boot:run -pl manual-runner -Dspring-boot.run.profiles=driver
mvn spring-boot:run -pl manual-runner -Dspring-boot.run.profiles=trip
mvn spring-boot:run -pl manual-runner -Dspring-boot.run.profiles=location
mvn spring-boot:run -pl manual-runner -Dspring-boot.run.profiles=diagnostic
```

### From IntelliJ

1. Open **Run > Edit Configurations**.
2. Select or create a **Spring Boot** configuration pointing to `ManualRunnerApplication`.
3. Set the **Active profiles** field to the domain you want to run (e.g. `vehicle`).
4. Run the configuration.

Only the matching runner will execute. No profile active = nothing runs.

## Structure

```
src/main/java/com/example/mongodb/runner/
  DomainRunner.java                       - interface all runners implement
  ManualRunnerApplication.java            - entry point; runs all active DomainRunner beans in order
  vehicle/VehicleRunner.java              - profile: vehicle  | exercises VehicleRepository
  driver/DriverRunner.java                - profile: driver   | exercises DriverRepository
  trip/TripRunner.java                    - profile: trip     | exercises TripRepository (seeds vehicle + driver, cleans up)
  location/LocationRunner.java            - profile: location | exercises LocationRepository (seeds trip, cleans up)
  diagnostic/DiagnosticEventRunner.java   - profile: diagnostic | exercises DiagnosticEventRepository (seeds vehicle, cleans up)
```

## Adding a new runner

1. Create a class in the relevant sub-package that implements `DomainRunner`.
2. Annotate it with `@Component` and `@Profile("<domain-name>")`.
3. Run it with `-Dspring-boot.run.profiles=<domain-name>`.

```java
@Component
@Profile("trip")
public class TripRunner implements DomainRunner {

    private final TripPersistencePort repository;

    public TripRunner(TripPersistencePort repository) {
        this.repository = repository;
    }

    @Override
    public void run() {
        // call repository methods here
    }
}
```

## Disabling a runner

Simply don't activate its profile. No profile active = no runner executes.
