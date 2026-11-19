package com.deveyk.orderflow.order.adapter.out.persistence.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface ProductSnapshotRepository extends JpaRepository<ProductSnapshotEntity, UUID> {
}
