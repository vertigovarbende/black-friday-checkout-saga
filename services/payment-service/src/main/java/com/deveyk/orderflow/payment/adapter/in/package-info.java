/**
 * payment-service · adapter.in
 *
 * Dış dünyadan gelen istekleri use case'e çeviren adapter'lar.
 *
 * web (controller, facade, request, response, mapper, filter), messaging (KafkaListener → inbox),
 * scheduling (zamanlanmış görevler). adapter.out'a bağımlı olamaz.
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §4
 */
package com.deveyk.orderflow.payment.adapter.in;
