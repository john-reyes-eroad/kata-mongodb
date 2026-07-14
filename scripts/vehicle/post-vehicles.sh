#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
VIN="${VIN:-VIN-EXPLORATORY-001}"
MAKE="${MAKE:-COVESA}"
MODEL="${MODEL:-Telematics-Unit}"
YEAR="${YEAR:-2024}"

curl -sS -X POST "$BASE_URL/api/vehicles" \
  -H "Content-Type: application/json" \
  -d "{\"vin\":\"$VIN\",\"make\":\"$MAKE\",\"model\":\"$MODEL\",\"year\":$YEAR}"
echo
