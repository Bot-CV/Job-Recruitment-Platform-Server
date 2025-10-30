package org.toanehihi.jobrecruitmentplatformserver.application.candidate.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.analytics.service.AnalyticService;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Candidate;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.CandidateSkill;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobApplication;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.SavedJob;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Skill;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.candidate.CandidateMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.JobApplicationMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.SavedJobMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.LocationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.SavedJobRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.SkillRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicationResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.skill.CandidateSkillRequest;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final LocationRepository locationRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final SavedJobRepository savedJobRepository;
    private final ResourceRepository resourceRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final LocationMapper locationMapper;
    private final CandidateMapper candidateMapper;
    private final SavedJobMapper savedJobMapper;
    private final ResourceMapper resourceMapper;
    private final JobApplicationMapper jobApplicationMapper;
    private final CurrentAccountProvider currentAccountProvider;
    private final CloudStorageService cloudStorageService;
    private final AnalyticService analyticService;

    @Override
    public CandidateResponse getProfile() {
        return candidateMapper.toResponse(getCurrentCandidate());
    }

    @Override
    @Transactional
    public CandidateResponse updateProfile(CandidateRequest request) {
        Candidate candidate = getCurrentCandidate();

        // Update location
        if (candidate.getLocation() != null) {
            Optional<Location> locationOpt = locationRepository.findById(candidate.getLocation().getId());
            if (locationOpt.isPresent()) {
                Location existLoc = locationOpt.get();
                locationMapper.updateLocation(existLoc, request.getLocation());
                locationRepository.save(existLoc);
            }
        } else {
            Location location = locationMapper.toLocation(request.getLocation());
            candidate.setLocation(location);
            locationRepository.save(location);
        }

        // Update skill
        Set<CandidateSkill> updatedSkills = new HashSet<>();
        for (CandidateSkillRequest skillRequest : request.getSkills()) {
            Skill skill = skillRepository.findByName(skillRequest.getSkillName())
                    .orElseGet(() -> skillRepository.save(Skill.builder()
                            .name(skillRequest.getSkillName())
                            .dateCreated(OffsetDateTime.now())
                            .build()));

            CandidateSkill candidateSkill = CandidateSkill.builder()
                    .candidate(candidate)
                    .skill(skill)
                    .level(skillRequest.getLevel())
                    .build();
            updatedSkills.add(candidateSkill);
        }
        candidate.getSkills().clear();
        candidate.getSkills().addAll(updatedSkills);

        // Update others
        candidateMapper.updateCandidate(candidate, request);
        candidate.setDateUpdated(OffsetDateTime.now());
        Candidate savedCandidate = candidateRepository.save(candidate);

        return candidateMapper.toResponse(savedCandidate);
    }

    @Override
    public SavedJobResponse saveJob(Long jobId) {
        Candidate candidate = getCurrentCandidate();
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        if (savedJobRepository.existsByCandidateAndJob(candidate, job)) {
            throw new AppException(ErrorCode.JOB_ALREADY_SAVED_TO_THIS_ACCOUNT);
        }
        SavedJob savedJob = SavedJob.builder()
                .candidate(candidate)
                .job(job)
                .savedAt(OffsetDateTime.now())
                .build();
        SavedJob result = savedJobRepository.save(savedJob);
//        analyticService.trackJobSaved(candidate.getAccount().getId(), job.getId());
        return savedJobMapper.toResponse(result);
    }

    @Override
    @Transactional
    public void removeSavedJob(Long jobId) {
        Candidate candidate = getCurrentCandidate();
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        savedJobRepository.deleteByCandidateAndJob(candidate, job);
    }

    @Override
    @Transactional
    public ResourceResponse updateAvatar(MultipartFile avatar) {
        Candidate candidate = getCurrentCandidate();
        Optional<Resource> currentAvt = resourceRepository.findById(candidate.getAvatarResourceId());
        if (currentAvt.isPresent()) {
            resourceRepository.delete(currentAvt.get());
            cloudStorageService.deleteFile(currentAvt.get().getPublicId());
        }
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(avatar, "avatar");
        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .resourceType(ResourceType.AVATAR)
                .url(fileInfo.url())
                .publicId(fileInfo.publicId())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    @Transactional
    public JobApplicationResponse applyJob(Long jobId, MultipartFile cv) {
        Candidate candidate = getCurrentCandidate();

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(cv, "cv");

        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .resourceType(ResourceType.CV)
                .url(fileInfo.url())
                .publicId(fileInfo.publicId())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);

        JobApplication jobApplication = JobApplication.builder()
                .candidate(candidate)
                .job(job)
                .status(ApplicationStatus.SUBMITTED)
                .cvResourceId(savedResource.getId())
                .build();

        JobApplication savedJobApplication = jobApplicationRepository.save(jobApplication);

//        analyticService.trackJobApplied(candidate.getAccount().getId(), job.getId());
        return jobApplicationMapper.toResponse(savedJobApplication);
    }

    @Override
    public PageResult<JobApplicationResponse> getAllApplications(int page, int size, String sortBy, String sortDir) {
        Candidate candidate = getCurrentCandidate();

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<JobApplication> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable);
        Page<JobApplicationResponse> responsePage = applicationPage.map(jobApplicationMapper::toResponse);

        return PageResult.from(responsePage);
    }

    @Override
    public PageResult<SavedJobResponse> getAllSavedJobs(int page, int size, String sortBy, String sortDir) {
        Candidate candidate = getCurrentCandidate();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<SavedJob> savedJobPage = savedJobRepository.findByCandidateId(candidate.getId(), pageable);

        return PageResult.from(
                savedJobPage.map(savedJobMapper::toResponse));
    }

    // Private methods
    private Candidate getCurrentCandidate() {
        Account account = currentAccountProvider.getCurrentAccount();
        return candidateRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }
}
