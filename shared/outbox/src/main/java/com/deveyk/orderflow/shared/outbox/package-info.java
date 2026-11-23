/**
 * shared-outbox: OutboxEventPort (+ OutboxEventAdapter), outbox tablosu (traceparent dahil) ve periyodik temizlik. Spring Boot starter.
 *
 * Servise açık: config (OutboxProperties), port (OutboxEventPort).
 * Modülün içi: adapter, jpa.entity, jpa.repository, cleanup; public'tir ama servis kullanamaz (ArchUnit kural 23).
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §3.4.
 */
package com.deveyk.orderflow.shared.outbox;
