#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
KEYWORD="${KEYWORD:-}"

if [[ -n "$KEYWORD" ]]; then
  curl -sS -G "$BASE_URL/api/drivers/count" --data-urlencode "keyword=$KEYWORD"
else
  curl -sS -X GET "$BASE_URL/api/drivers/count"
fi
echo
