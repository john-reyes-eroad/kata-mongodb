#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"
KEYWORD="${KEYWORD:-}"

if [[ -z "$KEYWORD" ]]; then
  echo "Usage: KEYWORD=<search-term> $0"
  exit 1
fi

curl -sS -H "Authorization: $AUTH_HEADER" -G "$BASE_URL/api/drivers/search" \
  --data-urlencode "keyword=$KEYWORD"
echo
