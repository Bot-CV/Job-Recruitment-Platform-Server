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
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis.RedisStreamConfig;

import java.time.OffsetDateTime;
import java.util.Map;

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

            Long accountId = Long.valueOf(v.get("accountId"));
            Long jobId = v.get("jobId") == null ? null : Long.valueOf(v.get("jobId"));
            InteractionEventType eventType = InteractionEventType.valueOf(v.get("eventType"));
            OffsetDateTime occurredAt = OffsetDateTime.parse(v.get("occurredAt"));

            Map<String, Object> metadata = Jsons.toMap(v.get("metadata"));

            UserInteraction ui = UserInteraction.builder()
                    .externalId(recordId)
                    .accountId(accountId)
                    .jobId(jobId)
                    .eventType(eventType)
                    .metadata(metadata)
                    .occurredAt(occurredAt)
                    .build();

            interactionRepository.save(ui);

            redis.opsForStream().acknowledge(
                    RedisStreamConfig.INTERACTION_STREAM_KEY,
                    RedisStreamConfig.INTERACTION_GROUP,
                    record.getId());
            log.debug("Saved user interaction: accountId={}, jobId={}, eventType={}",
                    accountId, jobId, eventType);
        } catch (Exception ex) {
            try {
                var payload = record.getValue();
                var dlq = Map.of(
                        "_originalId", record.getId().getValue(),
                        "_reason", ex.getClass().getSimpleName() + ":" + ex.getMessage(),
                        "payload", Jsons.toJson(payload));
                redis.opsForStream().add(StreamRecords.mapBacked(dlq).withStreamKey(RedisStreamConfig.DLQ_STREAM_KEY));
                redis.opsForStream().acknowledge(
                        RedisStreamConfig.INTERACTION_STREAM_KEY,
                        RedisStreamConfig.INTERACTION_GROUP,
                        record.getId());
            } catch (Exception ex2) {
                log.error("Failed to push to DLQ, manual investigation needed. id={}", record.getId(), ex2);
            }
            log.warn("InteractionMessageListener failed. id={}, err={}", record.getId(), ex.getMessage());
        }
    }
}
