#!/usr/bin/env bash
# infra/debezium/*.json → Kafka Connect. Connector'ın adı dosyanın adıdır: order-outbox.json → order-outbox.
# PUT /connectors/<ad>/config: connector yoksa oluşturur, varsa ayarını günceller; tekrar çalıştırılabilir.
# Kullanım (repo kökünden): infra/debezium/register-connector.sh
set -euo pipefail

CONNECT_URL="${CONNECT_URL:-http://localhost:8083}"

# Git Bash'te curl'e Windows yolu gitmesin diye dosyalar bulunduğu klasörden, göreli adla okunur.
cd "$(dirname "$0")"

for file in *.json; do
  name="${file%.json}"
  response="$(curl -s -w '\n%{http_code}' -X PUT \
    -H 'Content-Type: application/json' \
    --data @"$file" \
    "$CONNECT_URL/connectors/$name/config")"
  status="${response##*$'\n'}"
  body="${response%$'\n'*}"

  if [[ "$status" == "200" || "$status" == "201" ]]; then
    echo "$name: OK ($status)"
  else
    echo "$name: FAILED ($status)" >&2
    echo "$body" >&2
    exit 1
  fi
done
