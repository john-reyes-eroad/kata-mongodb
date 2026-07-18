#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
VIN="${VIN:-VIN-EXPLORATORY-001}"
MAKE="${MAKE:-COVESA}"
MODEL="${MODEL:-Telematics-Unit}"
YEAR="${YEAR:-2024}"

curl -sS -H "Authorization: $AUTH_HEADER" -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{\"vin\":\"$VIN\",\"make\":\"$MAKE\",\"model\":\"$MODEL\",\"year\":$YEAR}"
echo
