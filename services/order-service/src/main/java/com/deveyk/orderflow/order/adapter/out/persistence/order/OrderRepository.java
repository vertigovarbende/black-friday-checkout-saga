package com.deveyk.orderflow.order.adapter.out.persistence.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @EntityGraph(attributePaths = "lines")
    Optional<OrderEntity> findWithLinesById(UUID id);
}
