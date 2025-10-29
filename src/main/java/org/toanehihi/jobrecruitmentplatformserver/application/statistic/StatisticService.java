package org.toanehihi.jobrecruitmentplatformserver.application.statistic;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.StatisticResponse;

public interface StatisticService {
    StatisticResponse getPlatformStatistics(Account account);
}
