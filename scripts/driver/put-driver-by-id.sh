#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
DRIVER_ID="${DRIVER_ID:-}"
DRIVER_NAME="${DRIVER_NAME:-Updated Exploratory Driver}"
LICENSE_NUMBER="${LICENSE_NUMBER:-LIC-EXPLORATORY-UPDATED-001}"

if [[ -z "$DRIVER_ID" ]]; then
  echo "Usage: DRIVER_ID=<driver-id> [DRIVER_NAME=...] [LICENSE_NUMBER=...] $0"
  exit 1
fi

curl -sS -X PUT "$BASE_URL/api/drivers/$DRIVER_ID" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$DRIVER_NAME\",\"licenseNumber\":\"$LICENSE_NUMBER\"}"
echo
