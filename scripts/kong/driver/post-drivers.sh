#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
DRIVER_NAME="${DRIVER_NAME:-Exploratory Driver}"
LICENSE_NUMBER="${LICENSE_NUMBER:-LIC-EXPLORATORY-001}"

curl -sS -H "Authorization: $AUTH_HEADER" -X POST "$BASE_URL/api/drivers" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$DRIVER_NAME\",\"licenseNumber\":\"$LICENSE_NUMBER\"}"
echo
