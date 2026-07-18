#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
VEHICLE_ID="${VEHICLE_ID:-}"

if [[ -z "$VEHICLE_ID" ]]; then
  echo "Usage: VEHICLE_ID=<vehicle-id> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -i -X DELETE "$BASE_URL/api/vehicles/$VEHICLE_ID"
echo
