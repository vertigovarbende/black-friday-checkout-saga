package com.deveyk.orderflow.shared.persistence.testsupport;

import com.deveyk.orderflow.shared.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Entity
@Table(name = "probe")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProbeEntity extends BaseEntity {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    public ProbeEntity(final UUID id, final String name) {
        this.id = id;
        this.name = name;
    }

    public void rename(final String name) {
        this.name = name;
    }
}
