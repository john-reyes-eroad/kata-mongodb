#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
LOCATION_ID="${LOCATION_ID:-}"

if [[ -z "$LOCATION_ID" ]]; then
  echo "Usage: LOCATION_ID=<location-id> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -X GET "$BASE_URL/api/locations/$LOCATION_ID"
echo
