#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOCATION_ID="${LOCATION_ID:-}"
TRIP_ID="${TRIP_ID:-}"
LATITUDE="${LATITUDE:--36.8485}"
LONGITUDE="${LONGITUDE:-174.7633}"
RECORDED_AT="${RECORDED_AT:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

if [[ -z "$LOCATION_ID" || -z "$TRIP_ID" ]]; then
  echo "Usage: LOCATION_ID=<location-id> TRIP_ID=<trip-id> [LATITUDE=...] [LONGITUDE=...] [RECORDED_AT=ISO] $0"
  exit 1
fi

curl -sS -X PUT "$BASE_URL/api/locations/$LOCATION_ID" \
  -H "Content-Type: application/json" \
  -d "{\"tripId\":\"$TRIP_ID\",\"latitude\":$LATITUDE,\"longitude\":$LONGITUDE,\"recordedAt\":\"$RECORDED_AT\"}"
echo
