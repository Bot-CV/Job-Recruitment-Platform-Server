package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic;

import lombok.Builder;
import lombok.Data;

import java.util.TreeMap;

@Builder
@Data
public class StatisticResponse {
    Long currentPublishJobCount;

    Long totalNewApplicationCount;

    Long totalPendingApplicationCount;

    TreeMap<Integer, Long> weeklyApplicationCount;
}
