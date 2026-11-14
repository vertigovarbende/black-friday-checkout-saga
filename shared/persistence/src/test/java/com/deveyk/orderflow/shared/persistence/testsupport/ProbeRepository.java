package com.deveyk.orderflow.shared.persistence.testsupport;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProbeRepository extends JpaRepository<ProbeEntity, UUID> {
}
