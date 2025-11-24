package org.toanehihi.jobrecruitmentplatformserver.application.analytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis.Jsons;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis.RedisStreamConfig;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interaction.InteractionEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RedisStreamInteractionPublisher implements InteractionEventPublisher {

    private final StringRedisTemplate redis;
    private static final long MAX_LEN = 5_000_000L;

    @Override
    public void publish(List<InteractionEvent> events) {
        for (InteractionEvent e : events) {
            Map<String, String> map = new HashMap<>();
            map.put("accountId", String.valueOf(e.getAccountId()));
            if (e.getJobId() != null)
                map.put("jobId", String.valueOf(e.getJobId()));
            map.put("eventType", e.getEventType().name());
            map.put("occurredAt", e.getOccurredAt().toString());
            map.put("metadata", Jsons.toJson(e.getMetadata()));

            MapRecord<String, String, String> record = StreamRecords
                    .mapBacked(map)
                    .withStreamKey(RedisStreamConfig.INTERACTION_STREAM_KEY);

            redis.opsForStream().add(
                    record,
                    RedisStreamCommands.XAddOptions
                            .maxlen(MAX_LEN)
                            .approximateTrimming(true));
        }
    }
}
