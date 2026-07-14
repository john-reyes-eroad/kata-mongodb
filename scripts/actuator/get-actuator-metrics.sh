#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"

curl -sS -X GET "$BASE_URL/actuator/metrics"
echo
