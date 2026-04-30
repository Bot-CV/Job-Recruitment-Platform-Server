package org.toanehihi.jobrecruitmentplatformserver.application.outbox.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.OutboxEvent;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.OutboxStatus;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.OutboxEventRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class OutboxEventServiceImpl implements OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Override
    @Transactional
    public OutboxEvent saveOutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload) {
        return saveOutboxEvent(aggregateType, aggregateId, eventType, payload, UUID.randomUUID());
    }

    @Override
    @Transactional
    public OutboxEvent saveOutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload,
            UUID traceId) {
        OutboxEvent event = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .traceId(traceId)
                .occurredAt(OffsetDateTime.now())
                .status(OutboxStatus.PENDING)
                .attempts(0)
                .build();

        OutboxEvent saved = outboxEventRepository.save(event);
        log.debug("Saved outbox event: id={}, aggregateType={}, aggregateId={}, eventType={}",
                saved.getId(), aggregateType, aggregateId, eventType);

        return saved;
    }
}
