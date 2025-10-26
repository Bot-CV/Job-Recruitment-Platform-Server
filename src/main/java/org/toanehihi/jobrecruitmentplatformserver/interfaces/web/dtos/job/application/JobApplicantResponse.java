package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application;

import lombok.Builder;
import lombok.Getter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import java.util.List;

@Builder
@Getter
public class JobApplicantResponse {
    private Long id;
    private Long jobId;
    private Long candidateId;
    private String candidateName;
    private String email;
    private String phone;
    private ApplicationStatus status;
    private List<ResourceResponse> resource;
}
