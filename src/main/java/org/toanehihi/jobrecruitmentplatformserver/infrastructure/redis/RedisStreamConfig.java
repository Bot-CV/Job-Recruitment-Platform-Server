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

    public static final String STREAM_KEY = "outbox-events";
    public static final String GROUP = "recommend-service-group";
    public static final String DLQ_STREAM_KEY = "outbox-events:DLQ";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    @Bean
    public ApplicationRunner ensureGroup(StringRedisTemplate template) {
        return args -> {
            try {
                template.opsForStream().createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
                log.info("Created stream group '{}' on '{}'", GROUP, STREAM_KEY);
            } catch (Exception e) {
                log.info("Stream group '{}' already exists on '{}'", GROUP, STREAM_KEY);
            }
        };
    }
}

