#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:9090}"
AUTH_HEADER="${AUTH_HEADER:-Bearer test-token}"

curl -sS -H "Authorization: $AUTH_HEADER" -X GET "$BASE_URL/actuator/info"
echo
