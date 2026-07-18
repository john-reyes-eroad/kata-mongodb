# Manual exploratory curl scripts

Each script maps to one API endpoint from `API_ENDPOINTS.md`.

Count scripts return only `{"count": <number>}`. Set `KEYWORD` to count matching documents, or
omit it to count all documents in that domain.

There are two script sets:

- Spring direct scripts under `scripts/spring/**` (default `http://localhost:8080`)
- Kong gateway scripts under `scripts/kong/**` (default `http://localhost:9090`)

Override base URL per command:

```bash
BASE_URL=http://localhost:8080 ./scripts/spring/vehicle/get-vehicles.sh
BASE_URL=http://localhost:9090 ./scripts/kong/vehicle/get-vehicles.sh
```

Kong scripts include:

```bash
AUTH_HEADER="Bearer test-token"
```

Override token per command:

```bash
AUTH_HEADER="Bearer <your-token>" ./scripts/kong/vehicle/get-vehicles.sh
```

## Rate limit notes

Each `/api/*` request consumes one token from the remote address's shared
50-requests-per-second quota. If a sequence of scripts exceeds the quota, the
API returns `429 Too Many Requests` with `Retry-After` and rate-limit headers.
Forwarded headers such as `X-Forwarded-For` are ignored.

## Vehicles

- Spring: `./scripts/spring/vehicle/get-vehicles.sh`
- Kong: `./scripts/kong/vehicle/get-vehicles.sh`
- `KEYWORD="vin-123" ./scripts/spring/vehicle/get-vehicles-search.sh`
- `KEYWORD="vin-123" ./scripts/spring/vehicle/get-vehicles-count.sh` (omit `KEYWORD` to count all)
- `VEHICLE_ID=<id> ./scripts/spring/vehicle/get-vehicle-by-id.sh`
- `VIN="VIN123" MAKE="COVESA" MODEL="UnitX" YEAR=2024 ./scripts/spring/vehicle/post-vehicles.sh`
- `VEHICLE_ID=<id> VIN="VIN124" MAKE="COVESA" MODEL="UnitX2" YEAR=2025 ./scripts/spring/vehicle/put-vehicle-by-id.sh`
- `VEHICLE_ID=<id> ./scripts/spring/vehicle/delete-vehicle-by-id.sh`

## Drivers

- Spring: `./scripts/spring/driver/get-drivers.sh`
- Kong: `./scripts/kong/driver/get-drivers.sh`
- `KEYWORD="jane" ./scripts/spring/driver/get-drivers-search.sh`
- `KEYWORD="jane" ./scripts/spring/driver/get-drivers-count.sh` (omit `KEYWORD` to count all)
- `DRIVER_ID=<id> ./scripts/spring/driver/get-driver-by-id.sh`
- `DRIVER_NAME="Jane Driver" LICENSE_NUMBER="LIC-123" ./scripts/spring/driver/post-drivers.sh`
- `DRIVER_ID=<id> DRIVER_NAME="Jane Updated" LICENSE_NUMBER="LIC-456" ./scripts/spring/driver/put-driver-by-id.sh`
- `DRIVER_ID=<id> ./scripts/spring/driver/delete-driver-by-id.sh`

## Trips

- Spring: `./scripts/spring/trip/get-trips.sh`
- Kong: `./scripts/kong/trip/get-trips.sh`
- `KEYWORD=<trip-or-related-id> ./scripts/spring/trip/get-trips-count.sh` (omit `KEYWORD` to count all)
- `TRIP_ID=<id> ./scripts/spring/trip/get-trip-by-id.sh`
- `VEHICLE_ID=<vehicle-id> DRIVER_ID=<driver-id> START_TIME=<iso> END_TIME=<iso> DISTANCE_KM=42.5 ./scripts/spring/trip/post-trips.sh`
- `TRIP_ID=<trip-id> VEHICLE_ID=<vehicle-id> DRIVER_ID=<driver-id> START_TIME=<iso> END_TIME=<iso> DISTANCE_KM=55 ./scripts/spring/trip/put-trip-by-id.sh`
- `TRIP_ID=<id> ./scripts/spring/trip/delete-trip-by-id.sh`

## Locations

- Spring: `./scripts/spring/location/get-locations.sh`
- Kong: `./scripts/kong/location/get-locations.sh`
- `KEYWORD=<location-or-trip-id> ./scripts/spring/location/get-locations-count.sh` (omit `KEYWORD` to count all)
- `LOCATION_ID=<id> ./scripts/spring/location/get-location-by-id.sh`
- `TRIP_ID=<trip-id> LATITUDE=-36.8485 LONGITUDE=174.7633 RECORDED_AT=<iso> ./scripts/spring/location/post-locations.sh`
- `LOCATION_ID=<location-id> TRIP_ID=<trip-id> LATITUDE=-36.8500 LONGITUDE=174.7650 RECORDED_AT=<iso> ./scripts/spring/location/put-location-by-id.sh`
- `LOCATION_ID=<id> ./scripts/spring/location/delete-location-by-id.sh`

## Diagnostic Events

- Spring: `./scripts/spring/diagnostic-events/get-diagnostic-events.sh`
- Kong: `./scripts/kong/diagnostic-events/get-diagnostic-events.sh`
- `KEYWORD="P0001" ./scripts/spring/diagnostic-events/get-diagnostic-events-count.sh` (omit `KEYWORD` to count all)
- `DIAGNOSTIC_EVENT_ID=<id> ./scripts/spring/diagnostic-events/get-diagnostic-event-by-id.sh`
- `VEHICLE_ID=<vehicle-id> CODE=P0001 SEVERITY=HIGH DESCRIPTION="Issue" OCCURRED_AT=<iso> ./scripts/spring/diagnostic-events/post-diagnostic-events.sh`
- `DIAGNOSTIC_EVENT_ID=<id> VEHICLE_ID=<vehicle-id> CODE=P0002 SEVERITY=MEDIUM DESCRIPTION="Updated issue" OCCURRED_AT=<iso> ./scripts/spring/diagnostic-events/put-diagnostic-event-by-id.sh`
- `DIAGNOSTIC_EVENT_ID=<id> ./scripts/spring/diagnostic-events/delete-diagnostic-event-by-id.sh`

## Actuator

- Spring:
  - `./scripts/spring/actuator/get-actuator-health.sh`
  - `./scripts/spring/actuator/get-actuator-info.sh`
  - `./scripts/spring/actuator/get-actuator-metrics.sh`
- Kong:
  - `./scripts/kong/actuator/get-actuator-health.sh`
  - `./scripts/kong/actuator/get-actuator-info.sh`
  - `./scripts/kong/actuator/get-actuator-metrics.sh`

## Seed data

- Spring:
  - `COUNT=10 ./scripts/spring/seed/seed-domains.sh` (or `./scripts/spring/seed/seed-domains.sh 10`)
- Kong:
  - `COUNT=10 AUTH_HEADER="Bearer test-token" ./scripts/kong/seed/seed-domains.sh` (or `./scripts/kong/seed/seed-domains.sh 10`)
- The script supports up to 10,000 records per domain. Values with unique constraints are scoped to each seed run, so the script is safe to rerun.
- Creates linked data for all telematics domains:
  - vehicles
  - drivers
  - trips
  - locations
  - diagnostic-events
