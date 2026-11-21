/**
 * shared-test-support: test taban sınıfları ({@code LogTrackerConfiguration}, {@code OrderflowUnitTest}),
 * {@code TestDataBuilder}, {@code MutableClock}, {@code ErrorCodeAssert}. Servisler test scope'uyla ekler; shared
 * modüller ekleyemez (01-paket-yapisi-kararlari §3.3, kural 5). ArchUnit kuralları shared-architecture'dadır.
 *
 * <p>Faz 2'de eklenenler: web araçları, {@code OrderflowContainers} (Postgres, Kafka, Debezium Connect) ve
 * {@code DebeziumConnectors}, mesajlaşma araçları ({@code OutboxAssert}, {@code MessageProbe}), {@code GoldenJson}.
 *
 * Ayrıntı: docs/decisions/07-test-altyapisi-kararlari.md §3.1.
 */
package com.deveyk.orderflow.shared.testsupport;
