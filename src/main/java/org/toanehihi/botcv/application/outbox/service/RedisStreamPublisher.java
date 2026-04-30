package org.toanehihi.botcv.application.outbox.service;

import org.toanehihi.botcv.domain.model.OutboxEvent;

public interface RedisStreamPublisher {
    /**
     * Publish an outbox event to Redis Stream
     * @param event OutboxEvent to publish
     * @return true if successful, false otherwise
     */
    boolean publishToStream(OutboxEvent event);
}
