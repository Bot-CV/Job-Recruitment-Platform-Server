package org.toanehihi.jobrecruitmentplatformserver.application.analytics.service;

import java.util.Map;

public interface AnalyticService {
    void trackSearchQuery(
            Long accountId,
            String query,
            Map<String, Object> metadata);

    void trackJobViewed(
            Long accountId,
            Long jobId);

    void trackJobApplied(
            Long accountId,
            Long jobId);

    void trackJobSaved(
            Long accountId,
            Long jobId);
}
