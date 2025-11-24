package org.toanehihi.jobrecruitmentplatformserver.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.toanehihi.jobrecruitmentplatformserver.application.analytics.service.InteractionMessageListener;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisStreamListenerContainerConfig {

    @Bean
    public StreamMessageListenerContainer<String, ?> interactionListenerContainer(
            RedisConnectionFactory cf,
            InteractionMessageListener listener) {
        var options = StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                .batchSize(200)
                .pollTimeout(Duration.ofMillis(200))
                .build();

        var container = StreamMessageListenerContainer.create(cf, options);

        // Listen to INTERACTION stream
        container.receive(
                Consumer.from(RedisStreamConfig.INTERACTION_GROUP, "interaction-consumer-1"),
                StreamOffset.create(RedisStreamConfig.INTERACTION_STREAM_KEY, ReadOffset.lastConsumed()),
                listener);

        container.start();
        return container;
    }
}
