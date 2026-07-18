#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
DRIVER_ID="${DRIVER_ID:-}"

if [[ -z "$DRIVER_ID" ]]; then
  echo "Usage: DRIVER_ID=<driver-id> $0"
  exit 1
fi

curl -sS -X GET "$BASE_URL/api/drivers/$DRIVER_ID"
echo
