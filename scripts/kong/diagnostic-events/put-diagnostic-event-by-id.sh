#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
DIAGNOSTIC_EVENT_ID="${DIAGNOSTIC_EVENT_ID:-}"
VEHICLE_ID="${VEHICLE_ID:-}"
CODE="${CODE:-P0001}"
SEVERITY="${SEVERITY:-MEDIUM}"
DESCRIPTION="${DESCRIPTION:-Updated exploratory diagnostic event}"
OCCURRED_AT="${OCCURRED_AT:-$(date -u +%Y-%m-%dT%H:%M:%SZ)}"

if [[ -z "$DIAGNOSTIC_EVENT_ID" || -z "$VEHICLE_ID" ]]; then
  echo "Usage: DIAGNOSTIC_EVENT_ID=<event-id> VEHICLE_ID=<vehicle-id> [CODE=...] [SEVERITY=...] [DESCRIPTION='...'] [OCCURRED_AT=ISO] $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -X PUT "$BASE_URL/api/diagnostic-events/$DIAGNOSTIC_EVENT_ID" \
  -H "Content-Type: application/json" \
  -d "{\"vehicleId\":\"$VEHICLE_ID\",\"code\":\"$CODE\",\"severity\":\"$SEVERITY\",\"description\":\"$DESCRIPTION\",\"occurredAt\":\"$OCCURRED_AT\"}"
echo
