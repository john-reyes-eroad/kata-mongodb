#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
COUNT="${COUNT:-100}"
if [[ $# -gt 0 ]]; then
  COUNT="$1"
fi

if ! [[ "$COUNT" =~ ^[0-9]+$ ]]; then
  echo "COUNT must be an integer (1-10000)." >&2
  exit 1
fi

if (( COUNT < 1 || COUNT > 10000 )); then
  echo "COUNT must be between 1 and 10000." >&2
  exit 1
fi

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required." >&2
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required." >&2
  exit 1
fi

post_json() {
  local endpoint="$1"
  local payload="$2"
  curl -sS -f -X POST "$BASE_URL$endpoint" \
    -H "Content-Type: application/json" \
    -d "$payload"
}

extract_id() {
  local response="$1"
  printf '%s' "$response" | jq -er '.id'
}

vehicle_ids=()
driver_ids=()
trip_ids=()
location_ids=()
diagnostic_ids=()
severities=("LOW" "MEDIUM" "HIGH")
run_token="$(date -u +%Y%m%d%H%M%S)-$RANDOM"

echo "Seeding $COUNT records per domain into $BASE_URL ..."

for i in $(seq 1 "$COUNT"); do
  suffix=$(printf "%03d" "$i")

  vehicle_payload="{\"vin\":\"COVESA-VIN-$run_token-$suffix\",\"make\":\"COVESA\",\"model\":\"Telematics-$suffix\",\"year\":2024}"
  vehicle_id="$(extract_id "$(post_json "/api/vehicles" "$vehicle_payload")")"
  vehicle_ids+=("$vehicle_id")

  driver_payload="{\"name\":\"Driver $run_token-$suffix\",\"licenseNumber\":\"LIC-$run_token-$suffix\"}"
  driver_id="$(extract_id "$(post_json "/api/drivers" "$driver_payload")")"
  driver_ids+=("$driver_id")
done

for i in $(seq 1 "$COUNT"); do
  idx=$((i - 1))
  now="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
  distance="$(awk "BEGIN { printf \"%.1f\", 20 + ($i * 0.7) }")"

  trip_payload="{\"vehicleId\":\"${vehicle_ids[$idx]}\",\"driverId\":\"${driver_ids[$idx]}\",\"startTime\":\"$now\",\"endTime\":\"$now\",\"distanceKm\":$distance}"
  trip_id="$(extract_id "$(post_json "/api/trips" "$trip_payload")")"
  trip_ids+=("$trip_id")
done

for i in $(seq 1 "$COUNT"); do
  idx=$((i - 1))
  suffix=$(printf "%03d" "$i")
  lat="$(awk "BEGIN { printf \"%.6f\", -36.850000 + ($i / 10000) }")"
  lon="$(awk "BEGIN { printf \"%.6f\", 174.760000 + ($i / 10000) }")"
  now="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

  location_payload="{\"tripId\":\"${trip_ids[$idx]}\",\"latitude\":$lat,\"longitude\":$lon,\"recordedAt\":\"$now\"}"
  location_id="$(extract_id "$(post_json "/api/locations" "$location_payload")")"
  location_ids+=("$location_id")

  severity="${severities[$((idx % 3))]}"
  diagnostic_payload="{\"vehicleId\":\"${vehicle_ids[$idx]}\",\"code\":\"P$(printf "%04d" "$i")\",\"severity\":\"$severity\",\"description\":\"Diagnostic event $suffix\",\"occurredAt\":\"$now\"}"
  diagnostic_id="$(extract_id "$(post_json "/api/diagnostic-events" "$diagnostic_payload")")"
  diagnostic_ids+=("$diagnostic_id")
done

echo "Seed complete."
echo "Created:"
echo "  Vehicles:          ${#vehicle_ids[@]}"
echo "  Drivers:           ${#driver_ids[@]}"
echo "  Trips:             ${#trip_ids[@]}"
echo "  Locations:         ${#location_ids[@]}"
echo "  Diagnostic events: ${#diagnostic_ids[@]}"
