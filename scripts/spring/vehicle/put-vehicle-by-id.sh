#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
VEHICLE_ID="${VEHICLE_ID:-}"
VIN="${VIN:-VIN-EXPLORATORY-UPDATED-001}"
MAKE="${MAKE:-COVESA}"
MODEL="${MODEL:-Telematics-Unit-Updated}"
YEAR="${YEAR:-2025}"

if [[ -z "$VEHICLE_ID" ]]; then
  echo "Usage: VEHICLE_ID=<vehicle-id> [VIN=...] [MAKE=...] [MODEL=...] [YEAR=...] $0"
  exit 1
fi

curl -sS -X PUT "$BASE_URL/api/vehicles/$VEHICLE_ID" \
  -H "Content-Type: application/json" \
  -d "{\"vin\":\"$VIN\",\"make\":\"$MAKE\",\"model\":\"$MODEL\",\"year\":$YEAR}"
echo
