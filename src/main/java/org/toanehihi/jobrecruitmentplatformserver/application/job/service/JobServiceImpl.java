package org.toanehihi.jobrecruitmentplatformserver.application.job.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.toanehihi.jobrecruitmentplatformserver.application.analytics.service.InteractionService;
import org.toanehihi.jobrecruitmentplatformserver.application.candidate.service.CandidateService;
import org.toanehihi.jobrecruitmentplatformserver.application.outbox.service.OutboxEventService;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.*;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.EmploymentType;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.SeniorityLevel;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.WorkMode;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.JobMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.*;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.HasAdminRole;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.HasRecruiterRole;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.UserProfileBasedResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobServiceImpl implements JobService {
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final LocationRepository locationRepository;
    private final JobRoleRepository jobRoleRepository;
    private final SkillRepository skillRepository;
    private final RecruiterRepository recruiterRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final InteractionService analyticService;
    private final OutboxEventService outboxEventService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    
    @Value("${app.search-service-url}")
    private String searchServiceUrl;

    @Override
    public JobDetailResponse getJobDetail(Long id) {
        JobDetailResponse jobDetailResponse = jobRepository.findById(id)
                .map(jobMapper::toJobDetailResponse)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

//        analyticService.trackJobViewed(null, id);
        return jobDetailResponse;
    }

    @Override
    public PageResult<JobResponse> getAllJobs(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<JobResponse> jobs = jobRepository.findAll(pageable)
                .map(jobMapper::toResponse);

        return PageResult.from(jobs);
    }

    @Override
    @Transactional
    @HasRecruiterRole
    public JobResponse createJob(Account account, CreateJobRequest request) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Job job = jobMapper.toEntity(request);

        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
        JobRole jobRole = jobRoleRepository.findById(request.getJobRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_ROLE_NOT_FOUND));

        Set<Skill> skills = request.getSkills().stream()
                .map(skill -> {
                    return skillRepository.findByName(skill)
                            .orElseGet(() -> {
                                Skill newSkill = new Skill();
                                newSkill.setName(skill);
                                return skillRepository.save(newSkill);
                            });
                })
                .collect(Collectors.toSet());

        job.setCompany(recruiter.getCompany());
        job.setSkills(skills);
        job.setLocation(location);
        job.setJobRole(jobRole);
        job.setDatePosted(OffsetDateTime.now());
        job.setStatus(request.isSaveAsDraft() ? JobStatus.DRAFT : JobStatus.PENDING);
        job.getDescription().setJob(job);

        job = jobRepository.save(job);
        jobDescriptionRepository.save(job.getDescription());
        
        // Save outbox event for job creation
        JobEventPayload eventPayload = jobMapper.toEventPayload(job);
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);
            outboxEventService.saveOutboxEvent("JOB", job.getId(), "CREATED", payload);
            log.debug("Saved outbox event for job creation: jobId={}", job.getId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for job creation: jobId={}, error={}", 
                    job.getId(), e.getMessage(), e);
        }
        
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Account account, Long id, UpdateJobRequest request) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (!job.getCompany().getId().equals(recruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }

        validateJobCanBeUpdated(job);
        updateJobBasicFields(job, request);
        updateJobRelations(job, request);
        updateJobDescription(job, request);

        jobRepository.save(job);
        
        // Save outbox event for job update
        JobEventPayload eventPayload = jobMapper.toEventPayload(job);
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);
            outboxEventService.saveOutboxEvent("JOB", job.getId(), "UPDATED", payload);
            log.debug("Saved outbox event for job update: jobId={}", job.getId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for job update: jobId={}, error={}", 
                    job.getId(), e.getMessage(), e);
        }
        
        return jobMapper.toResponse(job);
    }

    private void validateJobCanBeUpdated(Job job) {
        if (job.getStatus() == JobStatus.CANCELED) {
            throw new AppException(ErrorCode.JOB_CLOSED_CANNOT_UPDATE);
        }

        if (jobApplicationRepository.existsJobApplicationByJobId(job.getId())
                && (job.getStatus() == JobStatus.PUBLISHED)) {
            throw new AppException(ErrorCode.JOB_HAS_APPLICANTS_CANNOT_UPDATE);
        }
    }

    private void updateJobBasicFields(Job job, UpdateJobRequest request) {
        if (request.getTitle() != null) {
            job.setTitle(request.getTitle());
        }
        if (request.getSeniorityLevel() != null) {
            job.setSeniority(SeniorityLevel.valueOf(request.getSeniorityLevel()));
        }
        if (request.getEmploymentType() != null) {
            job.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType()));
        }
        if (request.getMinExperienceYears() != null) {
            job.setMinExperienceYears(request.getMinExperienceYears());
        }
        if (request.getWorkMode() != null) {
            job.setWorkMode(WorkMode.valueOf(request.getWorkMode()));
        }
        if (request.getSalaryMin() != null) {
            job.setSalaryMin(request.getSalaryMin());
        }
        if (request.getSalaryMax() != null) {
            job.setSalaryMax(request.getSalaryMax());
        }
        if (request.getCurrency() != null) {
            job.setCurrency(request.getCurrency());
        }
        if (request.getDateExpires() != null) {
            job.setDateExpires(request.getDateExpires());
        }
    }

    private void updateJobRelations(Job job, UpdateJobRequest request) {
        if (request.getJobRoleId() != null) {
            JobRole jobRole = jobRoleRepository.findById(request.getJobRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.JOB_ROLE_NOT_FOUND));
            job.setJobRole(jobRole);
        }

        if (request.getSkills() != null && !request.getSkills().isEmpty()) {
            Set<Skill> skills = request.getSkills().stream()
                    .map(skill -> {
                        return skillRepository.findByName(skill)
                                .orElseGet(() -> {
                                    Skill newSkill = new Skill();
                                    newSkill.setName(skill);
                                    return skillRepository.save(newSkill);
                                });
                    })
                    .collect(Collectors.toSet());
            job.setSkills(skills);
        }

        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                    .orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND));
            job.setLocation(location);
        }
    }

    private void updateJobDescription(Job job, UpdateJobRequest request) {
        JobDescription description = job.getDescription() != null
                ? job.getDescription()
                : new JobDescription();

        if (request.getSummary() != null) {
            description.setSummary(request.getSummary());
        }
        if (request.getResponsibilities() != null) {
            description.setResponsibilities(request.getResponsibilities());
        }
        if (request.getRequirements() != null) {
            description.setRequirements(request.getRequirements());
        }
        if (request.getNiceToHave() != null) {
            description.setNiceToHave(request.getNiceToHave());
        }
        if (request.getBenefits() != null) {
            description.setBenefits(request.getBenefits());
        }
        if (request.getHiringProcess() != null) {
            description.setHiringProcess(request.getHiringProcess());
        }
        if (request.getNotes() != null) {
            description.setNotes(request.getNotes());
        }

        description.setJob(job);
        job.setDescription(description);
    }

    @Override
    @Transactional
    public JobResponse cancelJob(Account account, Long id) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if (!job.getCompany().getId().equals(recruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }
        if (job.getStatus() != JobStatus.PUBLISHED && job.getStatus() != JobStatus.PENDING) {
            throw new AppException(ErrorCode.JOB_CLOSED_CANNOT_UPDATE);
        }
        job.setStatus(JobStatus.CANCELED);
        log.info(job.getStatus().toString());
        
        job = jobRepository.save(job);
        
        // Save outbox event for job cancellation
        JobEventPayload eventPayload = jobMapper.toEventPayload(job);
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);
            outboxEventService.saveOutboxEvent("JOB", job.getId(), "UPDATED", payload);
            log.debug("Saved outbox event for job cancellation: jobId={}", job.getId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for job cancellation: jobId={}, error={}", 
                    job.getId(), e.getMessage(), e);
        }
        
        return jobMapper.toResponse(job);
    }

    @Override
    @Transactional
    @HasAdminRole
    public JobResponse moderateJobPosting(Account account, Long id, String action) {
        if (!account.getRole().getName().equals("ADMIN")) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }

        action = action.toUpperCase();

        if (!action.equals("APPROVE") && !action.equals("REJECT")) {
            throw new AppException(ErrorCode.INVALID_REQUEST_DATA);
        }
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if (job.getStatus() != JobStatus.PENDING) {
            throw new AppException(ErrorCode.JOB_NOT_IN_PENDING_STATUS);
        }
        job.setStatus(action.equals("APPROVE") ? JobStatus.PUBLISHED : JobStatus.CANCELED);
        job = jobRepository.save(job);
        
        // Save outbox event for job moderation
        JobEventPayload eventPayload = jobMapper.toEventPayload(job);
        try {
            String payload = objectMapper.writeValueAsString(eventPayload);
            outboxEventService.saveOutboxEvent("JOB", job.getId(), "UPDATED", payload);
            log.debug("Saved outbox event for job moderation: jobId={}, action={}", job.getId(), action);
        } catch (Exception e) {
            log.error("Failed to save outbox event for job moderation: jobId={}, action={}, error={}", 
                    job.getId(), action, e.getMessage(), e);
        }
        
        return jobMapper.toResponse(job);
    }

    @Override
    public PageResult<JobResponse> searchJobByTitle(JobSearchRequest request) {
        try {
            // Setup HTTP headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Wrap request body in HttpEntity
            HttpEntity<JobSearchRequest> httpEntity = new HttpEntity<>(request, headers);

            // Make POST request with proper entity
            JobSearchServiceResponse response = restTemplate.postForObject(
                    searchServiceUrl,
                    httpEntity,
                    JobSearchServiceResponse.class
            );

            List<JobResponse> jobs = new ArrayList<>();
            if (!response.getJobIds().isEmpty()) {
                List<Job> rows = jobRepository.findAllById(response.getJobIds());
                Map<Long, JobResponse> jobMap = rows.stream()
                        .map(jobMapper::toResponse)
                        .collect(Collectors.toMap(JobResponse::getId, Function.identity()));

                jobs = response.getJobIds().stream()
                        .map(jobMap::get)
                        .collect(Collectors.toList());
            }


            return PageResult.<JobResponse>builder()
                    .content(jobs)
                    .size(response.getPagination().getLimit())
                    .totalElements(jobs.size())
                    .hasNext(response.getPagination().isHasNext())
                    .hasPrevious(response.getPagination().isHasPrev())
                    .build();
        } catch (RestClientException e) {
            log.error("Error calling search service at {}: {}", searchServiceUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.SYSTEM_INTERNAL_ERROR);
        }
    }

    @Override
    public List<JobResponse> recommendJobs(Account account) {
        Candidate candidate = candidateRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));
        UserProfileBasedResponse userProfile = candidateService.getUserProfileBasedData(candidate.getId());
        return List.of();
    }

    @Override
    @Transactional
    public void deleteJob(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        
        // Save outbox event for job deletion before deleting
        try {
            JobEventPayload eventPayload = jobMapper.toEventPayload(job);
            String payload = objectMapper.writeValueAsString(eventPayload);
            outboxEventService.saveOutboxEvent("JOB", job.getId(), "DELETED", payload);
            log.debug("Saved outbox event for job deletion: jobId={}", job.getId());
        } catch (Exception e) {
            log.error("Failed to save outbox event for job deletion: jobId={}, error={}", 
                    job.getId(), e.getMessage(), e);
        }
        
        jobRepository.deleteById(id);
    }

    @Override
    public List<JobMetadataResponse> getJobMetadata(Set<Long> jobIds) {
        return jobRepository.findAllById(jobIds).stream()
                .map(job -> JobMetadataResponse.builder()
                        .jobId(job.getId())
                        .requiredSkills(
                                job.getSkills().stream()
                                .map(Skill::getName)
                                .collect(Collectors.toSet()))
                        .build())
                .toList();
    }

    @Override
    public List<JobResponse> getJobsRecommend(Long userId) {

        return List.of();
    }
}
