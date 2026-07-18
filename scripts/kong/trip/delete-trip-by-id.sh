#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
TRIP_ID="${TRIP_ID:-}"

if [[ -z "$TRIP_ID" ]]; then
  echo "Usage: TRIP_ID=<trip-id> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -i -X DELETE "$BASE_URL/api/trips/$TRIP_ID"
echo
