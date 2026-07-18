#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
DRIVER_ID="${DRIVER_ID:-}"

if [[ -z "$DRIVER_ID" ]]; then
  echo "Usage: DRIVER_ID=<driver-id> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -i -X DELETE "$BASE_URL/api/drivers/$DRIVER_ID"
echo
