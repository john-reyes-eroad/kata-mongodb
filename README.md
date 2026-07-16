# kata-mongodb

Spring Boot telematics CRUD API backed by MongoDB, using the MongoDB Java driver directly (no Spring Data MongoDB repository abstraction).

## Architecture

The API follows a hexagonal structure:

- `application`: framework-free domain records, ports, commands, services, and application exceptions
- `adapter-inbound-rest`: HTTP controllers, request DTOs, and API exception handling
- `adapter-outbound-mongodb`: MongoDB repositories, client configuration, and index initialization
- `bootstrap`: Spring Boot application and dependency wiring
- Inbound REST adapters: controllers and request DTOs under `com.example.mongodb.adapter.inbound.<domain>`
- Application inbound ports: use case interfaces under `*/port/inbound/` (`*UseCase`)
- Application commands and services: use-case inputs and implementations under `*/application/`
- Application outbound ports: persistence interfaces under `*/port/outbound/` (`*PersistencePort`)
- Outbound MongoDB adapters: repositories under `com.example.mongodb.adapter.outbound.<domain>`

This keeps HTTP and Mongo concerns at the edges while business flows are driven through ports.

## Tech stack

- Java 25
- Spring Boot 4.1
- MongoDB
- MongoDB Java sync driver (`mongodb-driver-sync`)

## Domains

- Vehicles
- Drivers
- Trips
- Locations
- Diagnostic Events

## Prerequisites

- Java 25
- Maven
- Docker + Docker Compose
- GraalVM 25 with `native-image` (native builds only)

## Run with Docker Compose

```bash
docker compose up -d --build
```

App URL:

```bash
http://localhost:8080
```

Health endpoint:

```bash
http://localhost:8080/actuator/health
```

Stop:

```bash
docker compose down
```

The Compose image is built for the container platform and runs the native
`kata-mongodb` executable. Its final distroless runtime contains only the
native executable, the required native libraries and CA certificates, plus a
small BusyBox health-check probe - it does not contain a JRE or shell. The
application runs as an unprivileged user.

## JVM build and run

The default Maven build remains a JVM build:

```bash
mvn package
java -jar bootstrap/target/bootstrap-0.0.1-SNAPSHOT.jar
```

Start MongoDB first. Default Mongo config is in
`bootstrap/src/main/resources/application.yml`.

## Native build and run

Use GraalVM 25 as `JAVA_HOME` and ensure `native-image --version` succeeds.
The native profile is intentionally configured only on the executable
`bootstrap` module. It activates Spring Boot AOT processing, produces
`bootstrap/target/kata-mongodb`, and preserves the normal JVM packaging when
the profile is absent.

```bash
mvn -pl bootstrap -am -Pnative native:compile
./bootstrap/target/kata-mongodb
```

The Spring AOT output supplies application reflection and proxy metadata, and
the MongoDB driver supplies its own native-image configuration. The only
application hint registers Hibernate Validator's generated logging classes and
the concrete validators used by the REST request DTOs, all of which Hibernate
Validator resolves at runtime. The profile disables the external
reachability-metadata repository because its current schema is newer than the
supported GraalVM 25 schema; Spring AOT and dependency-bundled metadata are
used instead.

## Container settings

Docker Compose limits the application container to one CPU and 512 MiB of
memory. The native executable starts directly, so JVM heap and direct-memory
settings do not apply.

Build and inspect the native image:

```bash
docker compose build app
docker compose images app
```

Both Compose services use Docker's `local` logging driver with three 10 MiB rotated log files.

## API reference

See `API_ENDPOINTS.md` for all endpoints by domain.

Every domain also provides `GET /api/<domain>/count?keyword=<keyword>`. It returns only
`{"count": <number>}`. Omit `keyword`, or provide an empty or whitespace-only value, to count all
documents in that domain.

## Manual exploratory scripts

Use scripts under `scripts/` for per-endpoint curl calls.

Script usage guide:

- `scripts/README.md`

## Seed sample data

```bash
COUNT=100 ./scripts/seed/seed-domains.sh
```

`COUNT` supports `1..10000`. Seed values with unique constraints include a run token, so the
script can be run repeatedly against the same database.

## Blackbox tests

Blackbox tests live in `blackbox-tests/` and exercise every endpoint documented in
`API_ENDPOINTS.md`: CRUD operations, searches, count queries, and actuator endpoints. The tests
remove records they create after each test.

Run:

```bash
mvn -f blackbox-tests/pom.xml test
```

Set `API_BASE_URL` to target a non-default running API:

```bash
API_BASE_URL=http://localhost:8080 mvn -f blackbox-tests/pom.xml test
```

## Learning guide

If your background is PostgreSQL, use:

- `MONGODB_FOR_POSTGRESQL_TUTORIAL.md`
