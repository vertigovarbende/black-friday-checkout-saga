/**
 * shared-kafka: servislerin ortak Kafka altyapısı. Error handler (bloklayan retry + &lt;topic&gt;.&lt;group&gt;.DLT,
 * sınıflandırma 06 §8), topic ayarları (KafkaTopicProperties) ve consumer / producer varsayılanları
 * (KafkaDefaultsEnvironmentPostProcessor). Spring Boot starter.
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §3, docs/decisions/06-exception-kararlari.md §8.
 */
package com.deveyk.orderflow.shared.kafka;
