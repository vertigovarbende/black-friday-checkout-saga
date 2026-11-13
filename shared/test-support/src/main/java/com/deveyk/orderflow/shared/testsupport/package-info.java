/**
 * shared-test-support: test taban sınıfları ({@code LogTrackerConfiguration}, {@code OrderflowUnitTest}),
 * {@code TestDataBuilder}, {@code MutableClock}, {@code ErrorCodeAssert}. Servisler test scope'uyla ekler; shared
 * modüller ekleyemez (01-paket-yapisi-kararlari §3.3, kural 5). ArchUnit kuralları shared-architecture'dadır.
 *
 * <p>Web araçları, {@code OrderflowContainers} ve mesajlaşma araçları Faz 2'de eklenir (ROADMAP).
 *
 * Ayrıntı: docs/decisions/07-test-altyapisi-kararlari.md §3.1.
 */
package com.deveyk.orderflow.shared.testsupport;
