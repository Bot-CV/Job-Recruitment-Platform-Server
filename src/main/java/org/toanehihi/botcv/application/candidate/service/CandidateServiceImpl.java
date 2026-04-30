package org.toanehihi.botcv.application.candidate.service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.cloud.service.CloudStorageService;
import org.toanehihi.botcv.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.domain.model.Candidate;
import org.toanehihi.botcv.domain.model.CandidateSkill;
import org.toanehihi.botcv.domain.model.Job;
import org.toanehihi.botcv.domain.model.JobApplication;
import org.toanehihi.botcv.domain.model.Location;
import org.toanehihi.botcv.domain.model.Resource;
import org.toanehihi.botcv.domain.model.SavedJob;
import org.toanehihi.botcv.domain.model.Skill;
import org.toanehihi.botcv.domain.model.enums.ApplicationStatus;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.infrastructure.persistence.mappers.candidate.CandidateMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.job.JobApplicationMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.job.SavedJobMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.LocationRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.SavedJobRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.SkillRepository;
import org.toanehihi.botcv.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.UserProfileBasedResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.application.JobApplicationResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResumeAnalysisResponse;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillRequest;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Slf4j
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
    private final JobApplicationMapper jobApplicationMapper;
    private final CurrentAccountProvider currentAccountProvider;
    private final CloudStorageService cloudStorageService;
    private final ResourceMapper resourceMapper;

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
    @Transactional
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
    @Transactional(rollbackFor = Exception.class)
    public JobApplicationResponse applyJob(Long jobId, MultipartFile cv) {
        Candidate candidate = getCurrentCandidate();

        Job job = jobRepository.findById(jobId).orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(cv, "cv");

        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .resourceType(ResourceType.CV)
                .contentType(fileInfo.contentType())
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
        return jobApplicationMapper.toResponse(savedJobApplication);
    }

    @Override
    public PageResult<JobApplicationResponse> getAllApplications(int page, int size, String sortBy, String sortDir) {
        Candidate candidate = getCurrentCandidate();

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<JobApplication> applicationPage = jobApplicationRepository.findByCandidate(candidate, pageable);
        Page<JobApplicationResponse> responsePage = applicationPage.map(jobApplicationMapper::toResponse);

        return PageResult.from(responsePage);
    }

    @Override
    public PageResult<SavedJobResponse> getAllSavedJobs(int page, int size, String sortBy, String sortDir) {
        Candidate candidate = getCurrentCandidate();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<SavedJob> savedJobPage = savedJobRepository.findByCandidateId(candidate.getId(), pageable);

        return PageResult.from(
                savedJobPage.map(savedJobMapper::toResponse));
    }

    @Override
    public PageResult<ResourceResponse> getCandidateResumes(int page, int size, String sortBy, String sortDir) {
        Candidate candidate = getCurrentCandidate();
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ResourceResponse> resourcePage = resourceRepository
                .findByOwnerIdAndResourceType(candidate.getId(), ResourceType.CV, pageable)
                .map(resourceMapper::toResponse);

        return PageResult.from(resourcePage);
    }

    @Override
    public UserProfileBasedResponse getUserProfileBasedData(Long candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));

        Set<String> skills = new HashSet<>();
        Set<String> educations = new HashSet<>();
        Set<String> locations = new HashSet<>();
        if (!candidate.getSkills().isEmpty()) {
            skills.addAll(candidate.getSkills().stream().map(cs -> cs.getSkill().getName()).toList());
        }
        if (candidate.getLocation().getProvinceCity() != null) {
            locations.add(candidate.getLocation().getProvinceCity());
        }

        return UserProfileBasedResponse.builder()
                .id(candidate.getId())
                .skills(skills)
                .educations(educations)
                .location(locations)
                .preferences(Map.of(
                        "remote", candidate.getRemotePref() != null && candidate.getRemotePref(),
                        "relocation", candidate.getRelocationPref() != null && candidate.getRelocationPref()))
                .build();
    }

    // Private methods
    private Candidate getCurrentCandidate() {
        Account account = currentAccountProvider.getCurrentAccount();
        return candidateRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }

    @Override
    public void updateProfileFromCV(Long accountId, ResumeAnalysisResponse cvData) {
        Candidate candidate = candidateRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));

        int years = cvData.getParsedYearsOfExperience();
        if (years > 0) {
            SeniorityLevel newLevel = calculateSeniority(years);
            SeniorityLevel currentLevel = candidate.getSeniority();

            if (currentLevel == null || newLevel.ordinal() > currentLevel.ordinal()) {
                candidate.setSeniority(newLevel);
            }
        } else if (candidate.getSeniority() == null) {
            candidate.setSeniority(SeniorityLevel.INTERN);
        }

        if (cvData.getSkills() != null && !cvData.getSkills().isEmpty()) {
            updateCandidateSkills(candidate, cvData.getSkills());
        }

        String extractedInfo = buildBioFromCV(cvData);
        if (!extractedInfo.isEmpty()) {
            String currentBio = candidate.getBio();

            if (currentBio == null || currentBio.isBlank()) {
                candidate.setBio(extractedInfo);
            } else {
                String normCurrent = currentBio.replaceAll("\\s+", "").toLowerCase();
                String normNew = extractedInfo.replaceAll("\\s+", "").toLowerCase();
                if (!normCurrent.contains(normNew)) {
                    candidate.setBio(currentBio + "\n\n" + extractedInfo);
                }
            }
        }

        candidateRepository.save(candidate);
    }

    private SeniorityLevel calculateSeniority(int years) {
        if (years < 1)
            return SeniorityLevel.FRESHER;
        if (years < 2)
            return SeniorityLevel.JUNIOR;
        if (years < 4)
            return SeniorityLevel.MID;
        if (years < 6)
            return SeniorityLevel.SENIOR;
        return SeniorityLevel.MANAGER;
    }

    private void updateCandidateSkills(Candidate candidate, List<String> skillNames) {
        Set<String> existingSkillNames = candidate.getSkills().stream()
                .map(cs -> cs.getSkill().getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        for (String rawName : skillNames) {
            if (rawName != null && !rawName.isBlank()) {
                String cleanName = rawName.trim();
                String lowerCaseName = cleanName.toLowerCase();

                if (!existingSkillNames.contains(lowerCaseName)) {
                    Skill skill = skillRepository.findByNameIgnoreCase(cleanName)
                            .orElseGet(() -> {
                                Skill newSkill = new Skill();
                                newSkill.setName(cleanName);
                                newSkill.setDateCreated(OffsetDateTime.now());
                                return skillRepository.save(newSkill);
                            });

                    CandidateSkill candidateSkill = new CandidateSkill();
                    candidateSkill.setCandidate(candidate);
                    candidateSkill.setSkill(skill);
                    candidateSkill.setLevel(1);

                    candidate.getSkills().add(candidateSkill);

                    existingSkillNames.add(lowerCaseName);
                }
            }
        }
    }

    private String buildBioFromCV(ResumeAnalysisResponse cv) {
        StringBuilder sb = new StringBuilder();

        appendIfPresent(sb, "Worked as: ", cv.getJobTitles());
        appendIfPresent(sb, "At companies: ", cv.getCompanies());
        appendIfPresent(sb, "Education: ", cv.getUniversities());

        return sb.toString();
    }

    private void appendIfPresent(StringBuilder sb, String prefix, List<String> values) {
        if (values != null && !values.isEmpty()) {
            String content = String.join(", ", new HashSet<>(values));
            sb.append(prefix).append(content).append(".\n");
        }
    }
}
