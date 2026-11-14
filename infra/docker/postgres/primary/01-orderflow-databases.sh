#!/bin/bash

set -Eeo pipefail

echo "host replication all all scram-sha-256" >> "$PGDATA/pg_hba.conf"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<SQL
CREATE ROLE replicator WITH LOGIN REPLICATION PASSWORD '${REPLICATOR_PASSWORD}';
CREATE ROLE debezium WITH LOGIN REPLICATION PASSWORD '${DEBEZIUM_PASSWORD}';
SELECT pg_create_physical_replication_slot('replica_1');
SQL

for service in order orchestrator product payment notification; do
  database="${service}_service"
  # Lokal ortam: kullanıcı adı = DB adı = şifre.
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<SQL
CREATE ROLE ${database} WITH LOGIN PASSWORD '${database}';
CREATE DATABASE ${database} OWNER ${database};
REVOKE CONNECT ON DATABASE ${database} FROM PUBLIC;
GRANT CONNECT ON DATABASE ${database} TO debezium;
SQL
done
