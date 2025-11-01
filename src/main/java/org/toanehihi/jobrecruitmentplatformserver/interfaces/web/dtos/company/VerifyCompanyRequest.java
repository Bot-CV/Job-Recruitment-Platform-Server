package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company;

import org.springframework.lang.Nullable;

import lombok.Getter;

@Getter
public class VerifyCompanyRequest {
    private Long companyId;
    private boolean isApproved;

    @Nullable
    private String reason;
}
