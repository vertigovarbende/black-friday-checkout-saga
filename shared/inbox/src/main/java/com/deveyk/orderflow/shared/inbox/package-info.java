/**
 * shared-inbox: InboxProcessor (inbox kaydı + iş + varsa outbox tek transaction'da, ADR-0006), InboxEventPort,
 * inbox tablosu ve periyodik temizlik. Spring Boot starter.
 *
 * Servise açık: config (InboxProperties), port (InboxEventPort, InboxMessage), support (InboxProcessor).
 * Modülün içi: adapter, jpa.entity, jpa.repository, cleanup; public'tir ama servis kullanamaz (ArchUnit kural 23).
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §3.4, docs/decisions/08-loglama-kararlari.md §5.2.
 */
package com.deveyk.orderflow.shared.inbox;
