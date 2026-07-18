#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
KEYWORD="${KEYWORD:-}"

if [[ -n "$KEYWORD" ]]; then
  curl -sS -H "Authorization: $AUTH_HEADER" -G "$BASE_URL/api/diagnostic-events/count" --data-urlencode "keyword=$KEYWORD"
else
  curl -sS -H "Authorization: $AUTH_HEADER" -X GET "$BASE_URL/api/diagnostic-events/count"
fi
echo
