package org.toanehihi.botcv.interfaces.web.dtos.company;

import org.springframework.lang.Nullable;

import lombok.Getter;

@Getter
public class VerifyCompanyRequest {
    private Long companyId;
    private boolean approved;

    @Nullable
    private String reason;
}
