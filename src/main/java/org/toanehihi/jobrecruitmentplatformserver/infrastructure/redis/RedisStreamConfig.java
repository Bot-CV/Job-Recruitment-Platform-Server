package org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
public class RedisStreamConfig {

    // Stream cho Outbox events (Job CREATED/UPDATED/DELETED)
    public static final String OUTBOX_STREAM_KEY = "outbox-events";
    public static final String OUTBOX_GROUP = "outbox-processor-group";

    // Stream cho User Interaction events (CLICK/APPLY/SAVE)
    public static final String INTERACTION_STREAM_KEY = "user-interactions";
    public static final String INTERACTION_GROUP = "interaction-processor-group";

    public static final String DLQ_STREAM_KEY = "dlq-stream";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public ApplicationRunner ensureStreamGroups(StringRedisTemplate template) {
        return args -> {
            // Create Interaction stream group
            createStreamGroup(template, INTERACTION_STREAM_KEY, INTERACTION_GROUP);

            // Create Outbox stream group (cho Python consumer)
            createStreamGroup(template, OUTBOX_STREAM_KEY, OUTBOX_GROUP);

            // Create DLQ stream group
            createStreamGroup(template, DLQ_STREAM_KEY, "dlq-consumer-group");
        };
    }

    private void createStreamGroup(StringRedisTemplate template, String streamKey, String groupName) {
        try {
            template.opsForStream().createGroup(streamKey, ReadOffset.latest(), groupName);
            log.info("Created stream group '{}' on stream '{}'", groupName, streamKey);
        } catch (Exception e) {
            log.info("Stream group '{}' already exists on stream '{}'", groupName, streamKey);
        }
    }
}
