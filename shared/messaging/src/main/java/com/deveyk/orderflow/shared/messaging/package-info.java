/**
 * shared-messaging: Event zarfı (OrderflowEvent, EventMetadata), EventBuilder, EventJsonReader, payload kontratları ve
 * mesaj tanımları (TopicNames, EventDefinitions). Taşıyıcıdan (Kafka, Redis) bağımsızdır; Kafka'ya özel olan shared-kafka'da.
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §3, docs/messaging/event-catalog.md.
 */
package com.deveyk.orderflow.shared.messaging;
