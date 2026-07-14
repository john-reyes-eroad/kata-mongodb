# Manual exploratory curl scripts

Each script maps to one API endpoint from `API_ENDPOINTS.md`.

Default base URL:

```bash
http://localhost:8080
```

Override per command:

```bash
BASE_URL=http://localhost:8080 ./scripts/vehicle/get-vehicles.sh
```

## Vehicles

- `./scripts/vehicle/get-vehicles.sh`
- `KEYWORD="vin-123" ./scripts/vehicle/get-vehicles-search.sh`
- `VEHICLE_ID=<id> ./scripts/vehicle/get-vehicle-by-id.sh`
- `VIN="VIN123" MAKE="COVESA" MODEL="UnitX" YEAR=2024 ./scripts/vehicle/post-vehicles.sh`
- `VEHICLE_ID=<id> VIN="VIN124" MAKE="COVESA" MODEL="UnitX2" YEAR=2025 ./scripts/vehicle/put-vehicle-by-id.sh`
- `VEHICLE_ID=<id> ./scripts/vehicle/delete-vehicle-by-id.sh`

## Drivers

- `./scripts/driver/get-drivers.sh`
- `KEYWORD="jane" ./scripts/driver/get-drivers-search.sh`
- `DRIVER_ID=<id> ./scripts/driver/get-driver-by-id.sh`
- `DRIVER_NAME="Jane Driver" LICENSE_NUMBER="LIC-123" ./scripts/driver/post-drivers.sh`
- `DRIVER_ID=<id> DRIVER_NAME="Jane Updated" LICENSE_NUMBER="LIC-456" ./scripts/driver/put-driver-by-id.sh`
- `DRIVER_ID=<id> ./scripts/driver/delete-driver-by-id.sh`

## Trips

- `./scripts/trip/get-trips.sh`
- `TRIP_ID=<id> ./scripts/trip/get-trip-by-id.sh`
- `VEHICLE_ID=<vehicle-id> DRIVER_ID=<driver-id> START_TIME=<iso> END_TIME=<iso> DISTANCE_KM=42.5 ./scripts/trip/post-trips.sh`
- `TRIP_ID=<trip-id> VEHICLE_ID=<vehicle-id> DRIVER_ID=<driver-id> START_TIME=<iso> END_TIME=<iso> DISTANCE_KM=55 ./scripts/trip/put-trip-by-id.sh`
- `TRIP_ID=<id> ./scripts/trip/delete-trip-by-id.sh`

## Locations

- `./scripts/location/get-locations.sh`
- `LOCATION_ID=<id> ./scripts/location/get-location-by-id.sh`
- `TRIP_ID=<trip-id> LATITUDE=-36.8485 LONGITUDE=174.7633 RECORDED_AT=<iso> ./scripts/location/post-locations.sh`
- `LOCATION_ID=<location-id> TRIP_ID=<trip-id> LATITUDE=-36.8500 LONGITUDE=174.7650 RECORDED_AT=<iso> ./scripts/location/put-location-by-id.sh`
- `LOCATION_ID=<id> ./scripts/location/delete-location-by-id.sh`

## Diagnostic Events

- `./scripts/diagnostic-events/get-diagnostic-events.sh`
- `DIAGNOSTIC_EVENT_ID=<id> ./scripts/diagnostic-events/get-diagnostic-event-by-id.sh`
- `VEHICLE_ID=<vehicle-id> CODE=P0001 SEVERITY=HIGH DESCRIPTION="Issue" OCCURRED_AT=<iso> ./scripts/diagnostic-events/post-diagnostic-events.sh`
- `DIAGNOSTIC_EVENT_ID=<id> VEHICLE_ID=<vehicle-id> CODE=P0002 SEVERITY=MEDIUM DESCRIPTION="Updated issue" OCCURRED_AT=<iso> ./scripts/diagnostic-events/put-diagnostic-event-by-id.sh`
- `DIAGNOSTIC_EVENT_ID=<id> ./scripts/diagnostic-events/delete-diagnostic-event-by-id.sh`

## Actuator

- `./scripts/actuator/get-actuator-health.sh`
- `./scripts/actuator/get-actuator-info.sh`
- `./scripts/actuator/get-actuator-metrics.sh`

## Seed data

- `COUNT=100 ./scripts/seed/seed-domains.sh` (or `./scripts/seed/seed-domains.sh 100`)
- Creates linked data for all telematics domains:
  - vehicles
  - drivers
  - trips
  - locations
  - diagnostic-events
