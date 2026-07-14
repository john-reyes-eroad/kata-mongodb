# kata-mongodb

Spring Boot telematics CRUD API backed by MongoDB, using the MongoDB Java driver directly (no Spring Data MongoDB repository abstraction).

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

## Run with Docker Compose

```bash
docker-compose up -d --build
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
docker-compose down
```

## Run locally without Docker

Start MongoDB first, then:

```bash
mvn spring-boot:run
```

Default Mongo config is in `src/main/resources/application.yml`.

## API reference

See `API_ENDPOINTS.md` for all endpoints by domain.

## Manual exploratory scripts

Use scripts under `scripts/` for per-endpoint curl calls.

Script usage guide:

- `scripts/README.md`

## Seed sample data

```bash
COUNT=100 ./scripts/seed/seed-domains.sh
```

`COUNT` supports `1..100`.

## Blackbox tests

Blackbox tests live in `blackbox-tests/`.

Run:

```bash
cd blackbox-tests
mvn test
```

## Learning guide

If your background is PostgreSQL, use:

- `MONGODB_FOR_POSTGRESQL_TUTORIAL.md`
