#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
DRIVER_NAME="${DRIVER_NAME:-Exploratory Driver}"
LICENSE_NUMBER="${LICENSE_NUMBER:-LIC-EXPLORATORY-001}"

curl -sS -X POST "$BASE_URL/api/drivers" \
  -H "Content-Type: application/json" \
  -d "{\"name\":\"$DRIVER_NAME\",\"licenseNumber\":\"$LICENSE_NUMBER\"}"
echo
