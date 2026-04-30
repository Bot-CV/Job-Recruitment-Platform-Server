package org.toanehihi.botcv.application.outbox.service;

import org.toanehihi.botcv.domain.model.OutboxEvent;

public interface OutboxEventService {
    /**
     * Save an outbox event within the current transaction
     * @param aggregateType Type of aggregate (e.g., "JOB", "CANDIDATE", "COMPANY")
     * @param aggregateId ID of the aggregate
     * @param eventType Type of event (e.g., "CREATED", "UPDATED", "DELETED")
     * @param payload JSON payload containing event data
     * @return Saved OutboxEvent
     */
    OutboxEvent saveOutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload);
    
    /**
     * Save an outbox event with trace ID
     * @param aggregateType Type of aggregate
     * @param aggregateId ID of the aggregate
     * @param eventType Type of event
     * @param payload JSON payload
     * @param traceId Trace ID for distributed tracing
     * @return Saved OutboxEvent
     */
    OutboxEvent saveOutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload, java.util.UUID traceId);
}
