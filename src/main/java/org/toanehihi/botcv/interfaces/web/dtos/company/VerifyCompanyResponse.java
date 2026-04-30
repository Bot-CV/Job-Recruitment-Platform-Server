package org.toanehihi.botcv.interfaces.web.dtos.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyCompanyResponse {
    private Long companyId;
    private boolean approved;
    private String reason;
}
