package org.toanehihi.botcv.interfaces.web.dtos.job.application;

import java.time.OffsetDateTime;

import org.toanehihi.botcv.domain.model.enums.ApplicationStatus;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.JobResponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobApplicationResponse {
    private Long id;
    private Long candidateId;
    private CompanyResponse company;
    private JobResponse jobResponse;
    private ApplicationStatus status;
    private ResourceResponse resource;
    private OffsetDateTime appliedAt;
}
