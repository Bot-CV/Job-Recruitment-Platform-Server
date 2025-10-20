package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobApplication;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicationResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JobApplicationMapper {
    private final JobApplicationRepository jobApplicationRepository;
    private final JobMapper jobMapper;
    private final CompanyMapper companyMapper;

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
        return JobApplicantResponse.builder()
                .id(jobApplication.getId())
                .candidateId(jobApplication.getCandidate().getId())
                .candidateName(jobApplication.getCandidate().getFullName())
                .email(jobApplication.getCandidate().getAccount().getEmail())
                .phone(jobApplication.getCandidate().getPhone())
                .resource(jobApplication.getResources().stream()
                        .map(resource -> ResourceResponse.builder()
                                .id(resource.getId())
                                .type(resource.getType())
                                .url(resource.getUrl())
                                .fileName(resource.getFileName())
                                .build())
                        .toList())
                .build();
    }
}
