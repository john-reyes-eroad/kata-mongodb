#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
VEHICLE_ID="${VEHICLE_ID:-}"
CODE="${CODE:-P0001}"
SEVERITY="${SEVERITY:-HIGH}"
DESCRIPTION="${DESCRIPTION:-Exploratory diagnostic event}"
OCCURRED_AT="${OCCURRED_AT:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

if [[ -z "$VEHICLE_ID" ]]; then
  echo "Usage: VEHICLE_ID=<vehicle-id> [CODE=P0001] [SEVERITY=HIGH] [DESCRIPTION='...'] [OCCURRED_AT=ISO] $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -X POST "$BASE_URL/api/diagnostic-events" \
  -H "Content-Type: application/json" \
  -d "{\"vehicleId\":\"$VEHICLE_ID\",\"code\":\"$CODE\",\"severity\":\"$SEVERITY\",\"description\":\"$DESCRIPTION\",\"occurredAt\":\"$OCCURRED_AT\"}"
echo
