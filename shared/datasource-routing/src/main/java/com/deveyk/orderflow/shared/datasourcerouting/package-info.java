/**
 * shared-datasource-routing: primary ve replica için iki Hikari pool; read-only transaction'ları
 * replica'ya yönlendiren LazyConnectionDataSourceProxy.
 *
 * Ayrıntı: docs/adr/0004-tek-postgres-cluster-primary-replica.md
 */
package com.deveyk.orderflow.shared.datasourcerouting;
