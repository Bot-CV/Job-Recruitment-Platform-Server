package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobApplication;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicationResponse;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JobApplicationMapper {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public JobApplicationResponse toResponse(JobApplication jobApplication) {
        JobApplication ja = jobApplicationRepository.findWithDetailsById(jobApplication.getId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND));
        return JobApplicationResponse.builder()
                .id(jobApplication.getId())
                .candidateId(jobApplication.getCandidate().getId())
                .jobResponse(jobMapper.toResponse(jobApplication.getJob()))
                .company(companyMapper.toResponse(ja.getJob().getCompany()))
                .status(jobApplication.getStatus())
                .cvResourceId(jobApplication.getCvResourceId())
                .appliedAt(jobApplication.getAppliedAt())
                .build();
    }

    public JobApplicantResponse toApplicantResponse(JobApplication jobApplication) {
        List<Resource> resources = new ArrayList<>();
        Resource cvResource = resourceRepository.findByIdAndResourceType(jobApplication.getCvResourceId(), ResourceType.CV)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));

        Resource avatarResource = resourceRepository.findByIdAndResourceType(jobApplication.getCandidate().getAvatarResourceId(), ResourceType.AVATAR)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        resources.add(cvResource);
        resources.add(avatarResource);

        return JobApplicantResponse.builder()
                .id(jobApplication.getId())
                .jobId(jobApplication.getJob().getId())
                .candidateId(jobApplication.getCandidate().getId())
                .candidateName(jobApplication.getCandidate().getFullName())
                .email(jobApplication.getCandidate().getAccount().getEmail())
                .phone(jobApplication.getCandidate().getPhone())
                .status(jobApplication.getStatus())
                .resource(resources.stream()
                        .map(resourceMapper::toResponse)
                        .toList()
                )
                .build();
    }
}
