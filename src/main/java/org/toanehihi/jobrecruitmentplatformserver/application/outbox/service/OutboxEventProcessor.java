package org.toanehihi.jobrecruitmentplatformserver.application.outbox.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.OutboxEvent;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.OutboxStatus;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.OutboxEventRepository;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class OutboxEventProcessor {
    
    private final OutboxEventRepository outboxEventRepository;
    private final RedisStreamPublisher redisStreamPublisher;
    
    private static final int MAX_ATTEMPTS = 3;
    
    /**
     * Process pending outbox events and publish them to Redis Stream
     * Runs every 5 seconds
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    @Transactional
    public void processPendingEvents() {
        try {
            List<OutboxEvent> pendingEvents = outboxEventRepository.findPendingEvents(OutboxStatus.PENDING);
            
            if (pendingEvents.isEmpty()) {
                log.debug("No pending outbox events to process");
                return;
            }
            
            log.info("Processing {} pending outbox events", pendingEvents.size());
            
            int processed = 0;
            int failed = 0;
            
            for (OutboxEvent event : pendingEvents) {
                try {
                    boolean success = redisStreamPublisher.publishToStream(event);
                    
                    if (success) {
                        event.setStatus(OutboxStatus.SENT);
                        processed++;
                        log.debug("Successfully processed outbox event: id={}", event.getId());
                    } else {
                        event.setAttempts(event.getAttempts() + 1);
                        failed++;
                        log.warn("Failed to publish outbox event: id={}, attempts={}", 
                                event.getId(), event.getAttempts());
                        
                        if (event.getAttempts() >= MAX_ATTEMPTS) {
                            event.setStatus(OutboxStatus.DLQ);
                            log.error("Outbox event moved to DLQ after {} attempts: id={}", 
                                    MAX_ATTEMPTS, event.getId());
                        } else {
                            event.setStatus(OutboxStatus.FAILED);
                        }
                    }
                    
                    outboxEventRepository.save(event);
                    
                } catch (Exception e) {
                    event.setAttempts(event.getAttempts() + 1);
                    event.setStatus(event.getAttempts() >= MAX_ATTEMPTS ? OutboxStatus.DLQ : OutboxStatus.FAILED);
                    outboxEventRepository.save(event);
                    failed++;
                    log.error("Error processing outbox event: id={}, error={}", 
                            event.getId(), e.getMessage(), e);
                }
            }
            
            log.info("Outbox event processing completed: processed={}, failed={}", processed, failed);
            
        } catch (Exception e) {
            log.error("Error in outbox event processor: {}", e.getMessage(), e);
        }
    }
}
