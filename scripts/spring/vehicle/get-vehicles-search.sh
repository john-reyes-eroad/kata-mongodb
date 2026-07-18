#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
KEYWORD="${KEYWORD:-}"

if [[ -z "$KEYWORD" ]]; then
  echo "Usage: KEYWORD=<search-term> $0"
  exit 1
fi

curl -sS -G "$BASE_URL/api/vehicles/search" \
  --data-urlencode "keyword=$KEYWORD"
echo
