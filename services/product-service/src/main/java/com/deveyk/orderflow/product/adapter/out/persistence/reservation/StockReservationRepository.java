package com.deveyk.orderflow.product.adapter.out.persistence.reservation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface StockReservationRepository extends JpaRepository<StockReservationEntity, UUID> {

    @EntityGraph(attributePaths = "lines")
    Optional<StockReservationEntity> findWithLinesById(UUID id);
}
