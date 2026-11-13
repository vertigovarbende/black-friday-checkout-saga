/**
 * notification-service · application
 *
 * Use case'ler, handler'lar ve port arayüzleri.
 *
 * Spring'den sadece @Component ve @Transactional kullanılır. adapter package'larına bağımlı olamaz.
 * Alt package'lar aggregate başınadır: notification.usecase, notification.handler, notification.port.
 *
 * Ayrıntı: docs/decisions/01-paket-yapisi-kararlari.md §4
 */
package com.deveyk.orderflow.notification.application;
