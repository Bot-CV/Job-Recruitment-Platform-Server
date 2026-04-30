package org.toanehihi.botcv.interfaces.web.dtos.statistic;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminStatisticResponse {
    private Long totalAccount;
    private Long totalCandidate;
    private Long totalRecruiter;
    private Long totalCompany;
    private Long totalJob;

    private Long pendingCompanyVerification;
    private Long pendingJobApproval;

    private Long weeklyNewAccount;
    private Long weeklyNewJob;
}
