package org.toanehihi.botcv.infrastructure.persistence.mappers.job;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.JobDescription;
import org.toanehihi.botcv.infrastructure.persistence.mappers.skill.SkillMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.interfaces.web.dtos.job.CreateJobRequest;
import org.toanehihi.botcv.interfaces.web.dtos.job.JobDetailResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.JobEventPayload;
import org.toanehihi.botcv.interfaces.web.dtos.job.JobResponse;

import lombok.RequiredArgsConstructor;

import org.toanehihi.botcv.domain.model.Job;
import org.toanehihi.botcv.domain.model.Location;
import org.toanehihi.botcv.domain.model.Resource;

@Component
@RequiredArgsConstructor
public class JobMapper {
    private final SkillMapper skillMapper;
    private final ResourceRepository resourceRepository;

    public JobResponse toResponse(Job job) {
        String companyLogoPublicId = null;
        if (job.getCompany() != null && job.getCompany().getLogoResourceId() != null) {
            companyLogoPublicId = resourceRepository.findById(job.getCompany().getLogoResourceId())
                    .map(Resource::getPublicId)
                    .orElse(null);
        }
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany().getName())
                .logo(companyLogoPublicId)
                .category(job.getCategory() != null ? job.getCategory().getName() : null)
                .seniority(job.getSeniority())
                .minExperienceYears(job.getMinExperienceYears())
                .location(extractLocation(job.getLocation()))
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .datePosted(job.getDatePosted())
                .dateExpires(job.getDateExpires())
                .status(job.getStatus())
                .maxCandidates(job.getMaxCandidates())
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .build();
    }

    public JobDetailResponse toJobDetailResponse(Job job) {
        return JobDetailResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany().getName())
                .category(job.getCategory() != null ? job.getCategory().getName() : null)
                .seniority(job.getSeniority())
                .minExperienceYears(job.getMinExperienceYears())
                .location(extractLocation(job.getLocation()))
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .datePosted(job.getDatePosted())
                .dateExpires(job.getDateExpires())
                .status(job.getStatus())
                .maxCandidates(job.getMaxCandidates())
                .responsibilities(
                        job.getDescription().getResponsibilities() != null ? job.getDescription().getResponsibilities()
                                : "")
                .requirements(job.getDescription().getRequirements())
                .niceToHave(job.getDescription().getNiceToHave())
                .benefits(job.getDescription().getBenefits())
                .hiringProcess(job.getDescription().getHiringProcess())
                .notes(job.getDescription().getNotes())
                .summary(job.getDescription().getSummary())
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .build();
    }

    public Job toEntity(CreateJobRequest request) {
        return Job.builder()
                .title(request.getTitle())
                .seniority(request.getSeniorityLevel())
                .employmentType(request.getEmploymentType())
                .minExperienceYears(request.getMinExperienceYears())
                .workMode(request.getWorkMode())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(request.getCurrency())
                .maxCandidates(request.getMaxCandidates())
                .dateExpires(request.getDateExpires())
                .description(JobDescription.builder()
                        .summary(request.getSummary())
                        .responsibilities(request.getResponsibilities())
                        .requirements(request.getRequirements())
                        .niceToHave(request.getNiceToHave())
                        .benefits(request.getBenefits())
                        .hiringProcess(request.getHiringProcess())
                        .notes(request.getNotes())
                        .build())
                .build();
    }

    public JobEventPayload toEventPayload(Job job) {
        return JobEventPayload.builder()
                .id(job.getId())
                .title(job.getTitle())
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .location(extractLocation(job.getLocation()))
                .description(buildDescription(job.getDescription()))
                .company(job.getCompany().getName())
                .category(job.getCategory() != null ? job.getCategory().getName() : null)
                .seniority(job.getSeniority())
                .minExperienceYears(job.getMinExperienceYears())
                .workMode(job.getWorkMode())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .currency(job.getCurrency())
                .status(job.getStatus())
                .maxCandidates(job.getMaxCandidates())
                .datePosted(job.getDatePosted())
                .dateExpires(job.getDateExpires())
                .build();
    }

    private String extractLocation(Location jobLocation) {
        if (jobLocation == null) {
            return null;
        }
        return jobLocation.getStreetAddress() + ", " +
                jobLocation.getWard() + ", " +
                jobLocation.getDistrict() + ", " +
                jobLocation.getProvinceCity() + ", " +
                jobLocation.getCountry();
    }

    private String buildDescription(JobDescription jobDescription) {
        return "Responsibilities: " + jobDescription.getResponsibilities() + "\n" +
                "Requirements: " + jobDescription.getRequirements() + "\n" +
                "Nice to have: " + jobDescription.getNiceToHave() + "\n";
    }
}
