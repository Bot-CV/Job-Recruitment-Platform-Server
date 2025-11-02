package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecruiterResponse {
    private Long id;
    private Long accountId;
    private String fullName;
    private String phone;
    private String email;
    private ResourceResponse resource;
    private CompanyResponse company;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
}
