package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.jobrecruitmentplatformserver.application.statistic.StatisticService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.AdminStatisticResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.StatisticResponse;

@RestController
@RequestMapping("api/statistics")
@RequiredArgsConstructor
public class StatisticController {
    private final StatisticService statisticService;

    @GetMapping("/recruiter")
    public DataResponse<StatisticResponse> getStatistics(@CurrentUser Account account) {
        return DataResponse.<StatisticResponse>builder()
                .data(statisticService.getPlatformStatistics(account))
                .build();
    }

    @GetMapping("/admin")
    public DataResponse<AdminStatisticResponse> getAdminStatistics(@CurrentUser Account account) {
        return DataResponse.<AdminStatisticResponse>builder()
                .data(statisticService.getAdminStatistics(account))
                .build();
    }
}
