package org.toanehihi.botcv.application.outbox.service;

import org.toanehihi.botcv.domain.model.OutboxEvent;

import java.util.UUID;

public interface OutboxEventService {
    OutboxEvent saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload);
    OutboxEvent saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload, UUID traceId);
}
