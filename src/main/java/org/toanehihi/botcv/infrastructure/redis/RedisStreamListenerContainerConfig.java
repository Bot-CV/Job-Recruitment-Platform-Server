package org.toanehihi.botcv.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.toanehihi.botcv.application.analytics.service.InteractionMessageListener;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisStreamListenerContainerConfig {

    private final StringRedisTemplate redisTemplate;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;

    @SuppressWarnings("unused")
    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> interactionListenerContainer(
            RedisConnectionFactory cf,
            InteractionMessageListener listener) {

        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                .<String, MapRecord<String, String, String>>builder()
                .batchSize(200)
                .pollTimeout(Duration.ofMillis(200))
                .build();

        container = StreamMessageListenerContainer.create(cf, options);

        // Listen to INTERACTION stream
        container.receive(
                Consumer.from(RedisStreamConfig.INTERACTION_GROUP, "interaction-consumer-1"),
                StreamOffset.create(RedisStreamConfig.INTERACTION_STREAM_KEY, ReadOffset.lastConsumed()),
                listener);

        return container;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startContainerAfterReady() {
        // Tạo stream groups trước
        ensureStreamGroups();

        // Sau đó mới start container
        if (container != null && !container.isRunning()) {
            container.start();
            log.info("Started InteractionMessageListener container");
        }
    }

    private void ensureStreamGroups() {
        createStreamGroupWithMkStream(RedisStreamConfig.INTERACTION_STREAM_KEY, RedisStreamConfig.INTERACTION_GROUP);
        createStreamGroupWithMkStream(RedisStreamConfig.OUTBOX_STREAM_KEY, RedisStreamConfig.OUTBOX_GROUP);
        createStreamGroupWithMkStream(RedisStreamConfig.DLQ_STREAM_KEY, "dlq-consumer-group");
    }

    private void createStreamGroupWithMkStream(String streamKey, String groupName) {
        try {
            redisTemplate.execute(connection -> {
                try {
                    connection.execute(
                            "XGROUP",
                            "CREATE".getBytes(),
                            streamKey.getBytes(),
                            groupName.getBytes(),
                            "$".getBytes(),
                            "MKSTREAM".getBytes());
                    log.info("Created stream '{}' with group '{}'", streamKey, groupName);
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                        log.info("Stream group '{}' already exists", groupName);
                    }
                }
                return null;
            }, true);
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
        }
    }
}
