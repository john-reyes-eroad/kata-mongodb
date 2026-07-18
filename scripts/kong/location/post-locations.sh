#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
TRIP_ID="${TRIP_ID:-}"
LATITUDE="${LATITUDE:--36.8485}"
LONGITUDE="${LONGITUDE:-174.7633}"
RECORDED_AT="${RECORDED_AT:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

if [[ -z "$TRIP_ID" ]]; then
  echo "Usage: TRIP_ID=<trip-id> [LATITUDE=-36.8485] [LONGITUDE=174.7633] [RECORDED_AT=ISO] $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -X POST "$BASE_URL/api/locations" \
  -H "Content-Type: application/json" \
  -d "{\"tripId\":\"$TRIP_ID\",\"latitude\":$LATITUDE,\"longitude\":$LONGITUDE,\"recordedAt\":\"$RECORDED_AT\"}"
echo
