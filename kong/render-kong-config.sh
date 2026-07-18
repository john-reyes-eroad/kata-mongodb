#!/bin/sh
set -eu

LUA_FILE="/kong/scripts/authorizer-pre-function.lua"
TEMPLATE_FILE="/kong/kong.yml"
OUTPUT_FILE="/tmp/kong.yml"

if [ ! -f "$LUA_FILE" ]; then
  echo "Lua script not found: $LUA_FILE" >&2
  exit 1
fi

if [ ! -f "$TEMPLATE_FILE" ]; then
  echo "Kong template not found: $TEMPLATE_FILE" >&2
  exit 1
fi

awk -v lua_file="$LUA_FILE" '
  /^[[:space:]]*__AUTHORIZER_LUA__$/ {
    while ((getline line < lua_file) > 0) {
      print "              " line
    }
    close(lua_file)
    injected = 1
    next
  }
  { print }
  END {
    if (!injected) {
      print "Lua placeholder __AUTHORIZER_LUA__ not found in template" > "/dev/stderr"
      exit 2
    }
  }
' "$TEMPLATE_FILE" > "$OUTPUT_FILE"
