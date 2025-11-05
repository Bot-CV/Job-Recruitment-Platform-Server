package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobDescription;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.skill.SkillMapper;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.CreateJobRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobDetailResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobEventPayload;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;

@Component
public class JobMapper {
    private final SkillMapper skillMapper;

    public JobMapper(SkillMapper skillMapper) {
        this.skillMapper = skillMapper;
    }

    public JobResponse toResponse(Job job){
        return JobResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany().getName())
                .jobRole(job.getJobRole().getName())
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
                .maxCandidates(job.getMaxCandidates() != null ? job.getMaxCandidates() : null)
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .build();
    }

    public JobDetailResponse toJobDetailResponse(Job job){
        return JobDetailResponse.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany().getName())
                .jobRole(job.getJobRole().getName())
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
                .maxCandidates(job.getMaxCandidates() != null ? job.getMaxCandidates() : null)
                .responsibilities(job.getDescription().getResponsibilities())
                .requirements(job.getDescription().getRequirements())
                .niceToHave(job.getDescription().getNiceToHave())
                .benefits(job.getDescription().getBenefits())
                .hiringProcess(job.getDescription().getHiringProcess())
                .notes(job.getDescription().getNotes())
                .summary(job.getDescription().getSummary())
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .build();
    }

    public Job toEntity(CreateJobRequest request){
        return Job.builder()
                .title(request.getTitle())
                .jobRole(null)
                .seniority(request.getSeniorityLevel())
                .employmentType(request.getEmploymentType())
                .minExperienceYears(request.getMinExperienceYears())
                .location(null)
                .workMode(request.getWorkMode())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .currency(request.getCurrency())
                .maxCandidates(request.getMaxCandidates() != null ? request.getMaxCandidates() : null)
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

    public JobEventPayload toEventPayload(Job job){
        return JobEventPayload.builder()
                .id(job.getId())
                .title(job.getTitle())
                .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
                .location(extractLocation(job.getLocation()))
                .description(buildDescription(job.getDescription()))
                .company(job.getCompany().getName())
                .jobRole(job.getJobRole().getName())
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

    private String extractLocation(Location jobLocation){
        if (jobLocation == null) {
            return null;
        }
        return jobLocation.getStreetAddress() + ", " +
                jobLocation.getWard() + ", " +
                jobLocation.getDistrict() + ", " +
                jobLocation.getProvinceCity() + ", " +
                jobLocation.getCountry();
    }
    private String buildDescription(JobDescription jobDescription){
        return "Responsibilities: " + jobDescription.getResponsibilities() + "\n" +
                "Requirements: " + jobDescription.getRequirements() + "\n" +
                "Nice to have: " + jobDescription.getNiceToHave() + "\n";
    }
}
