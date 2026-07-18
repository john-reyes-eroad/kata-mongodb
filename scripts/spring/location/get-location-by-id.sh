#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
LOCATION_ID="${LOCATION_ID:-}"

if [[ -z "$LOCATION_ID" ]]; then
  echo "Usage: LOCATION_ID=<location-id> $0"
  exit 1
fi

curl -sS -X GET "$BASE_URL/api/locations/$LOCATION_ID"
echo
