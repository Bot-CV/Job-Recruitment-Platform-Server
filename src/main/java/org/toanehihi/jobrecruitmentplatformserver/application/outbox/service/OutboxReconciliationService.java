package org.toanehihi.jobrecruitmentplatformserver.application.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.OutboxEvent;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.OutboxStatus;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.JobMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.OutboxEventRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobEventPayload;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class OutboxReconciliationService {

    private final JobRepository jobRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventService outboxEventService;
    private final RedisStreamPublisher redisStreamPublisher;
    private final JobMapper jobMapper;
    private final ObjectMapper objectMapper;

    /**
     * Reconcile jobs vs outbox events: if a job has no corresponding outbox event,
     * create one and publish to Redis Stream.
     */
    public void reconcileMissingJobEvents() {
        try {
            log.info("Starting outbox reconciliation for JOBs");

            // Collect all job IDs
            List<Job> allJobs = jobRepository.findAll()
                    .stream()
                    .filter(job -> job != null && (job.getStatus() == JobStatus.PENDING || job.getStatus() == JobStatus.PUBLISHED))
                    .toList();
            Set<Long> jobIds = new HashSet<>();
            for (Job job : allJobs) {
                jobIds.add(job.getId());
            }

            // Collect all aggregate IDs that already have any outbox event for JOB
            Set<Long> existingOutboxJobIds = outboxEventRepository.findDistinctAggregateIdsByAggregateType("JOB");

            // Find missing
            int created = 0;
            for (Job job : allJobs) {
                if (job == null || job.getId() == null) continue;
                if (existingOutboxJobIds != null && existingOutboxJobIds.contains(job.getId())) {
                    continue;
                }

                boolean ok = processSingleJob(job);
                if (ok) created++;
            }

            log.info("Outbox reconciliation completed. New events created: {}", created);
        } catch (Exception e) {
            log.error("Error during outbox reconciliation: {}", e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean processSingleJob(Job job) {
        try {
            // Refetch job with required relations inside this transactional context
            Job reloaded = jobRepository.findByIdWithRelations(job.getId()).orElse(job);

            JobEventPayload eventPayload = jobMapper.toEventPayload(reloaded);
            String payload = objectMapper.writeValueAsString(eventPayload);

            OutboxEvent event = outboxEventService.saveOutboxEvent("JOB", job.getId(), "CREATED", payload);

            boolean published = redisStreamPublisher.publishToStream(event);
            if (published) {
                event.setStatus(OutboxStatus.SENT);
            } else {
                event.setAttempts(event.getAttempts() + 1);
                event.setStatus(OutboxStatus.FAILED);
            }
            outboxEventRepository.save(event);
            log.info("Reconciled missing outbox event for jobId={}", job.getId());
            return true;
        } catch (Exception e) {
            log.error("Failed to reconcile outbox event for jobId={}, error={}", job.getId(), e.getMessage(), e);
            return false;
        }
    }
}


