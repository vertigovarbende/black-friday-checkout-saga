/**
 * notification-service · domain
 *
 * İş kuralları, aggregate'ler, value object'ler ve domain exception'ları.
 *
 * Saf Java: sadece JDK ve shared-domain. Spring, JPA, Jackson ve SLF4J yok; log yazılmaz.
 * Alt package'lar aggregate başınadır: notification.model, notification.exception, ortak value object'ler için common.
 *
 * Ayrıntı: docs/decisions/05-domain-kararlari.md
 */
package com.deveyk.orderflow.notification.domain;
