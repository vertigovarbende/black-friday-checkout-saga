#!/bin/bash
set -euo pipefail

if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo "replica: veri yok, primary'den kopyalanıyor"
  mkdir -p "$PGDATA"
  chown -R postgres:postgres /var/lib/postgresql
  chmod 700 "$PGDATA"
  until gosu postgres pg_isready -h postgres-primary -p 5432 -U replicator; do sleep 1; done
  gosu postgres env PGPASSWORD="$REPLICATOR_PASSWORD" \
    pg_basebackup -h postgres-primary -p 5432 -U replicator -D "$PGDATA" -X stream -R -S replica_1
fi

exec docker-entrypoint.sh postgres
