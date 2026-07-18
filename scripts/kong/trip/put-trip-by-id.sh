#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
TRIP_ID="${TRIP_ID:-}"
VEHICLE_ID="${VEHICLE_ID:-}"
DRIVER_ID="${DRIVER_ID:-}"
START_TIME="${START_TIME:-$(date -u -v-1H +%Y-%m-%dT%H:%M:%SZ 2>/dev/null || date -u -d '1 hour ago' +%Y-%m-%dT%H:%M:%SZ)}"
END_TIME="${END_TIME:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"
DISTANCE_KM="${DISTANCE_KM:-55.0}"

if [[ -z "$TRIP_ID" || -z "$VEHICLE_ID" || -z "$DRIVER_ID" ]]; then
  echo "Usage: TRIP_ID=<trip-id> VEHICLE_ID=<vehicle-id> DRIVER_ID=<driver-id> [START_TIME=ISO] [END_TIME=ISO] [DISTANCE_KM=55.0] $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -X PUT "$BASE_URL/api/trips/$TRIP_ID" \
  -H "Content-Type: application/json" \
  -d "{\"vehicleId\":\"$VEHICLE_ID\",\"driverId\":\"$DRIVER_ID\",\"startTime\":\"$START_TIME\",\"endTime\":\"$END_TIME\",\"distanceKm\":$DISTANCE_KM}"
echo
