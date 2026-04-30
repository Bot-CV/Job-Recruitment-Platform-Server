package org.toanehihi.botcv.infrastructure.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@Configuration
public class RedisStreamConfig {

    // Stream cho Outbox events
    public static final String OUTBOX_STREAM_KEY = "outbox-events";
    public static final String OUTBOX_GROUP = "outbox-processor-group";

    // Stream cho User Interaction events
    public static final String INTERACTION_STREAM_KEY = "user-interactions";
    public static final String INTERACTION_GROUP = "interaction-processor-group";

    public static final String DLQ_STREAM_KEY = "dlq-stream";

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
