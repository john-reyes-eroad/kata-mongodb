#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
VEHICLE_ID="${VEHICLE_ID:-}"

if [[ -z "$VEHICLE_ID" ]]; then
  echo "Usage: VEHICLE_ID=<vehicle-id> $0"
  exit 1
fi

curl -sS -X GET "$BASE_URL/api/vehicles/$VEHICLE_ID"
echo
