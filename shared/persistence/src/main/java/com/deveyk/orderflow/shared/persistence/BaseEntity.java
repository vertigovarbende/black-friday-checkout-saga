package com.deveyk.orderflow.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Aggregate köklerinin JPA entity'leri için temel sınıf (05-domain-kararlari §9).
 *
 * <ul>
 *   <li>{@code version}: optimistic locking. {@code Long}'dur, çünkü Spring Data yeni satırı
 *       {@code version == null} ile anlar; {@code long} olsaydı id'ye bakardı, id'lerimiz önceden atandığı için
 *       her kayıt "mevcut" sayılır ve INSERT'ten önce boşuna SELECT atılırdı.</li>
 *   <li>{@code createdAt} / {@code updatedAt}: satırın teknik zamanları, JPA auditing doldurur. Saat uygulamanın
 *       {@code Clock} bean'idir. Domain'in iş zamanlarından ({@code placedAt}, {@code lastModifiedAt}) ayrıdır.</li>
 * </ul>
 *
 * <p>Id her entity'nin kendisindedir. Aggregate içindeki satır entity'leri ({@code OrderLineEntity}) ve
 * outbox / inbox entity'leri bu sınıftan türemez.
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
