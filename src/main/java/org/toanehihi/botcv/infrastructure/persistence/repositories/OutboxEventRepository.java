package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.OutboxEvent;
import org.toanehihi.botcv.domain.model.enums.OutboxStatus;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Set;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.occurredAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status);

    boolean existsByAggregateTypeAndAggregateId(String aggregateType, Long aggregateId);

    @Query("SELECT DISTINCT e.aggregateId FROM OutboxEvent e WHERE e.aggregateType = :aggregateType")
    Set<Long> findDistinctAggregateIdsByAggregateType(@Param("aggregateType") String aggregateType);
}
