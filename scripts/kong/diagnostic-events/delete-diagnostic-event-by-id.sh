#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
DIAGNOSTIC_EVENT_ID="${DIAGNOSTIC_EVENT_ID:-}"

if [[ -z "$DIAGNOSTIC_EVENT_ID" ]]; then
  echo "Usage: DIAGNOSTIC_EVENT_ID=<event-id> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -i -X DELETE "$BASE_URL/api/diagnostic-events/$DIAGNOSTIC_EVENT_ID"
echo
