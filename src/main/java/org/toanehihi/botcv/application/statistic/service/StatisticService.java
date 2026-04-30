package org.toanehihi.botcv.application.statistic.service;

import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.interfaces.web.dtos.statistic.AdminStatisticResponse;
import org.toanehihi.botcv.interfaces.web.dtos.statistic.StatisticResponse;

public interface StatisticService {
    StatisticResponse getPlatformStatistics(Account account);

    AdminStatisticResponse getAdminStatistics(Account account);
}
