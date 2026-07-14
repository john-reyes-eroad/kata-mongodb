#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
TRIP_ID="${TRIP_ID:-}"

if [[ -z "$TRIP_ID" ]]; then
  echo "Usage: TRIP_ID=<trip-id> $0"
  exit 1
fi

curl -sS -X GET "$BASE_URL/api/trips/$TRIP_ID"
echo
