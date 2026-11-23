package com.deveyk.orderflow.shared.outbox.jpa.repository;

import com.deveyk.orderflow.shared.outbox.jpa.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Modifying
    @Query("delete from OutboxEventEntity e where e.createdAt < :cutoff")
    int deleteCreatedBefore(@Param("cutoff") Instant cutoff);

}
