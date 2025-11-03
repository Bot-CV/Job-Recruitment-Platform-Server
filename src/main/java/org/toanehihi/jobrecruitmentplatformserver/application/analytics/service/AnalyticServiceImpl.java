package org.toanehihi.jobrecruitmentplatformserver.application.analytics.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Analytic;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.EventType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AnalyticRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticServiceImpl implements AnalyticService {

    private final AnalyticRepository analyticRepository;
    private final ResourceRepository resourceRepository;
    private final CloudStorageService cloudStorageService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.nlp-service-url}")
    private String nlpServiceUrl;

    @Override
    public void trackSearchQuery(Long accountId, String query, Map<String, Object> metadata) {
        Analytic analytic = Analytic.builder()
                .accountId(accountId)
                .targetId(null)
                .eventType(EventType.SEARCH_QUERY)
                .metadata(metadata)
                .build();
        analyticRepository.save(analytic);
    }

    @Override
    public void trackJobViewed(Long accountId, Long jobId) {
        Analytic analytic = Analytic.builder()
                .accountId(accountId)
                .targetId(jobId)
                .eventType(EventType.JOB_VIEWED)
                .build();
        analyticRepository.save(analytic);
    }

    @Override
    public void trackJobApplied(Long accountId, Long jobId) {
        Analytic analytic = Analytic.builder()
                .accountId(accountId)
                .targetId(jobId)
                .eventType(EventType.JOB_APPLIED)
                .build();
        analyticRepository.save(analytic);
    }

    @Override
    public void trackJobSaved(Long accountId, Long jobId) {
        Analytic analytic = Analytic.builder()
                .accountId(accountId)
                .targetId(jobId)
                .eventType(EventType.JOB_SAVED)
                .build();
        analyticRepository.save(analytic);
    }
}
