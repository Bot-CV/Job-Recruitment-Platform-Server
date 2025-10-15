package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application;

import java.time.OffsetDateTime;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobApplicationResponse {
    private Long id;
    private Long candidateId;
    private CompanyResponse company;
    private JobResponse jobResponse;
    private ApplicationStatus status;
    private Long cvResourceId;
    private OffsetDateTime appliedAt;
}
