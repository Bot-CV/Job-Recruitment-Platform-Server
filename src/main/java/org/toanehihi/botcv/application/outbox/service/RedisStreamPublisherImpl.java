package org.toanehihi.botcv.application.outbox.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.model.OutboxEvent;
import org.toanehihi.botcv.infrastructure.redis.RedisStreamConfig;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisStreamPublisherImpl implements RedisStreamPublisher {

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean publishToStream(OutboxEvent event) {
        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("id", String.valueOf(event.getId()));
            fields.put("aggregateType", event.getAggregateType());
            fields.put("aggregateId", String.valueOf(event.getAggregateId()));
            fields.put("eventType", event.getEventType());
            fields.put("payload", event.getPayload());
            fields.put("occurredAt", event.getOccurredAt().toString());
            fields.put("traceId", event.getTraceId().toString());
            fields.put("attempts", String.valueOf(event.getAttempts()));

            var record = StreamRecords.newRecord()
                    .ofStrings(fields)
                    .withStreamKey(RedisStreamConfig.OUTBOX_STREAM_KEY);

            RecordId recordId = redisTemplate.opsForStream().add(record);

            log.info("Published outbox event to Redis Stream: eventId={}, recordId={}, streamKey={}",
                    event.getId(), recordId, RedisStreamConfig.OUTBOX_STREAM_KEY);

            return recordId != null;
        } catch (Exception e) {
            log.error("Failed to publish outbox event to Redis Stream: eventId={}, error={}",
                    event.getId(), e.getMessage(), e);
            return false;
        }
    }
}
