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
- Bucket4j and Caffeine for bounded per-client API rate limiting

## Domains

- Vehicles
- Drivers
- Trips
- Locations
- Diagnostic Events

## Prerequisites

- Docker + Docker Compose

## Run with Docker Compose

Choose one runtime flavor. Both profiles start MongoDB, floci, lambda-init,
Kong, and the app; do not start both profiles at once.

- Direct app URL: `http://localhost:8080`
- Kong gateway URL: `http://localhost:9090`

### Native image

```bash
docker compose --profile native up --build
```

This profile builds the GraalVM native executable and runs it in a distroless
image containing only the executable, required native libraries and CA
certificates, plus a small BusyBox health-check probe. It does not contain a
JRE or shell and runs as an unprivileged user.

Native image compilation requires Docker Desktop to have at least 6 GiB of
memory available to its build environment. Allocate 8 GiB when possible.
If the build fails at `Dockerfile.native` with `ResourceExhausted` or
`cannot allocate memory`, increase Docker Desktop's memory allocation and
retry. This is build-time memory for GraalVM, not the native container's
512 MiB runtime limit.

### Java runtime

```bash
docker compose --profile java up --build
```

This profile packages the Spring Boot jar and runs it with Eclipse Temurin
Java 25. It is useful when native-image build time or memory requirements are
not appropriate for the environment. ZGC is enabled by default for this profile
via `JAVA_TOOL_OPTIONS=-XX:+UseZGC`.

### Health and shutdown

In a separate terminal:

```bash
curl http://localhost:8080/actuator/health
```

Press Ctrl+C in the Compose terminal to stop the services. Remove the stopped
containers with:

```bash
docker compose down
```

Build either flavor without starting it:

```bash
docker compose --profile native build app-native
docker compose --profile java build app-java
```

## API rate limiting

All `/api/**` routes are limited independently for each client to 50 requests per
second. The token bucket has a capacity of 50 and refills its 50 tokens every
second, so a client can make an initial burst of 50 requests and then sustain 50
requests per second. Requests that exceed the available tokens receive
`429 Too Many Requests` with `Retry-After`, `RateLimit-Limit`,
`RateLimit-Remaining`, and `RateLimit-Reset` headers.

Client identity is the remote address observed by the application server.
Forwarded headers, including `X-Forwarded-For`, are ignored so callers cannot
select or spoof their quota. When the application runs behind a reverse proxy,
the limit applies to that proxy's remote address unless the deployment makes
the original client address available at the server transport layer.

Buckets are held in an in-memory Caffeine cache capped at 10,000 clients and
expire after 10 minutes without access. These values, along with the
requests-per-second limit, are configurable under `rate-limit` in
`bootstrap/src/main/resources/application.yml`. The filter is registered only
for `/api/*`, so actuator endpoints such as `/actuator/health` are not limited.

## Container settings

Docker Compose limits the application container to one CPU and 512 MiB of
memory. The native flavor starts directly, so JVM heap and direct-memory
settings do not apply to it.

Both Compose services use Docker's `local` logging driver with three 10 MiB rotated log files.

## API reference

See `API_ENDPOINTS.md` for all endpoints by domain.
For gateway authorization flow details, see `KONG_AWS_LAMBDA_AUTHORIZER.md`.
For manual-runner usage, see `manual-runner/README.md`.

Kong-proxied API calls require an `Authorization` header. For the current local
authorizer implementation, any value starting with `Bearer ` is accepted.

Every domain also provides `GET /api/<domain>/count?keyword=<keyword>`. It returns only
`{"count": <number>}`. Omit `keyword`, or provide an empty or whitespace-only value, to count all
documents in that domain.

## Manual exploratory scripts

Use scripts under `scripts/spring/` for direct app calls and `scripts/kong/`
for gateway calls with Bearer auth.

Script usage guide:

- `scripts/README.md`

## Seed sample data

```bash
COUNT=10 ./scripts/spring/seed/seed-domains.sh
```

`COUNT` supports `1..10000`. Seed values with unique constraints include a run token, so the
script can be run repeatedly against the same database.

## Blackbox tests

Blackbox tests live in `blackbox-tests/` and exercise every endpoint documented in
`API_ENDPOINTS.md`: CRUD operations, searches, count queries, request validation,
canonical not-found errors, response contracts, rate limiting, and actuator
endpoints. The tests pace ordinary API requests to stay within the limit, then use
concurrent bursts to verify the 429 response and that spoofed forwarded headers do
not create separate quotas. The tests remove records they create after each test.

Run:

```bash
mvn -f blackbox-tests/pom.xml test
```

Set `API_BASE_URL` to target a non-default running API:

```bash
API_BASE_URL=http://localhost:8080 mvn -f blackbox-tests/pom.xml test
```

The repository concurrency integration test is opt-in because it requires the
Compose MongoDB service. Run it as part of Java-runtime verification:

```bash
mvn -Pmongodb-integration-tests -pl adapter-outbound-mongodb -am test
mvn -f blackbox-tests/pom.xml test
```

## Learning guide

If your background is PostgreSQL, use:

- `MONGODB_FOR_POSTGRESQL_TUTORIAL.md`
