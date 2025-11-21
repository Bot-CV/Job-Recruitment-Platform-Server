package org.toanehihi.jobrecruitmentplatformserver.application.analytics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.UserInteraction;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InteractionEventType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.InteractionRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis.Jsons;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis.RedisStreamConfig.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InteractionMessageListener implements StreamListener<String, MapRecord<String, String, String>> {

    private final InteractionRepository interactionRepository;
    private final StringRedisTemplate redis;

    @Override
    public void onMessage(MapRecord<String, String, String> record) {
        try {
            String recordId = record.getId().getValue();
            Map<String, String> v = record.getValue();

            // Filter chỉ xử lý USER_INTERACTION events
            String aggregateType = v.get("aggregateType");
            if (!"USER_INTERACTION".equals(aggregateType)) {
                log.debug("Skipping non-interaction event: aggregateType={}, recordId={}", 
                         aggregateType, recordId);
                // Vẫn phải ACK để message không bị pending
                redis.opsForStream().acknowledge(STREAM_KEY, GROUP, record.getId());
                return;
            }
            
            // Parse từ payload thay vì trực tiếp từ record
            String payloadJson = v.get("payload");
            Map<String, Object> payload = Jsons.toMap(payloadJson);

            Long accountId = Long.valueOf(v.get("accountId"));
            Long jobId = v.get("jobId") == null ? null : Long.valueOf(v.get("jobId"));
            
            // eventType có thể ở payload hoặc top-level
            String eventTypeStr = payload.getOrDefault("interactionType", v.get("eventType")).toString();
            InteractionEventType eventType = InteractionEventType.valueOf(eventTypeStr);

            // occurredAt từ payload hoặc top-level
            String occurredAtStr = payload.getOrDefault("timestamp", v.get("occurredAt")).toString();
            OffsetDateTime occurredAt = OffsetDateTime.parse(occurredAtStr);

            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) payload.getOrDefault("metadata", Map.of());

            UserInteraction ui = UserInteraction.builder()
                    .externalId(recordId)
                    .accountId(accountId)
                    .jobId(jobId)
                    .eventType(eventType)
                    .metadata(metadata)
                    .occurredAt(occurredAt)
                    .build();

            interactionRepository.save(ui);

            redis.opsForStream().acknowledge(STREAM_KEY, GROUP, record.getId());
            log.debug("Saved user interaction: accountId={}, jobId={}, eventType={}", 
                     accountId, jobId, eventType);
        } catch (Exception ex) {
            try {
                var payload = record.getValue();
                var dlq = Map.of(
                        "_originalId", record.getId().getValue(),
                        "_reason", ex.getClass().getSimpleName() + ":" + ex.getMessage(),
                        "payload", Jsons.toJson(payload)
                );
                redis.opsForStream().add(StreamRecords.mapBacked(dlq).withStreamKey(DLQ_STREAM_KEY));
                redis.opsForStream().acknowledge(STREAM_KEY, GROUP, record.getId());
            } catch (Exception ex2) {
                log.error("Failed to push to DLQ, manual investigation needed. id={}", record.getId(), ex2);
            }
            log.warn("InteractionMessageListener failed. id={}, err={}", record.getId(), ex.getMessage());
        }
    }
}
