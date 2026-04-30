package org.toanehihi.botcv.application.recruiter.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.cloud.service.CloudStorageService;
import org.toanehihi.botcv.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.botcv.application.email.service.EmailService;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.*;
import org.toanehihi.botcv.domain.model.enums.ApplicationStatus;
import org.toanehihi.botcv.domain.model.enums.JobStatus;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.interview.InterviewMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.job.JobApplicationMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.job.JobMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.recruiter.RecruiterMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.*;
import org.toanehihi.botcv.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyLocationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.interview.CreateInterviewRequest;
import org.toanehihi.botcv.interfaces.web.dtos.interview.InterviewResponse;
import org.toanehihi.botcv.interfaces.web.dtos.interview.UpdateInterviewRequest;
import org.toanehihi.botcv.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.botcv.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.botcv.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecruiterServiceImpl implements RecruiterService {

    private final CurrentAccountProvider currentAccountProvider;
    private final RecruiterRepository recruiterRepository;
    private final CompanyRepository companyRepository;
    private final LocationRepository locationRepository;
    private final ResourceRepository resourceRepository;
    private final RecruiterMapper recruiterMapper;
    private final CompanyMapper companyMapper;
    private final ResourceMapper resourceMapper;
    private final CloudStorageService cloudStorageService;
    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobApplicationMapper jobApplicationMapper;
    private final InterviewMapper interviewMapper;
    private final InterviewRepository interviewRepository;
    private final EmailService emailService;

    @Override
    public RecruiterResponse getProfile() {
        return recruiterMapper.toResponse(getCurrentRecruiter());
    }

    @Override
    @Transactional
    public RecruiterResponse updateProfile(RecruiterRequest request) {
        Recruiter recruiter = getCurrentRecruiter();

        recruiter.setFullName(request.getFullName());
        Recruiter savedRecruiter = recruiterRepository.save(recruiter);
        return recruiterMapper.toResponse(savedRecruiter);
    }

    @Override
    @Transactional
    public CompanyResponse updateCompany(CompanyRequest request) {
        Recruiter recruiter = getCurrentRecruiter();

        Company company = companyRepository.findByRecruiter(recruiter)
                .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND));

        Set<CompanyLocation> updatedLocations = new HashSet<>();
        for (CompanyLocationRequest locationRequest : request.getCompanyLocations()) {
            Location location = Location.builder()
                    .streetAddress(locationRequest.getLocation().getStreetAddress())
                    .ward(locationRequest.getLocation().getWard())
                    .district(locationRequest.getLocation().getDistrict())
                    .provinceCity(locationRequest.getLocation().getProvinceCity())
                    .country(locationRequest.getLocation().getCountry())
                    .build();
            locationRepository.save(location);

            CompanyLocation companyLocation = CompanyLocation.builder()
                    .company(company)
                    .location(location)
                    .isHeadquarter(locationRequest.getIsHeadquarter())
                    .build();
            updatedLocations.add(companyLocation);
        }
        company.getCompanyLocations().clear();
        company.getCompanyLocations().addAll(updatedLocations);

        companyMapper.updateCompany(company, request);
        Company savedCompany = companyRepository.save(company);
        return companyMapper.toResponse(savedCompany);
    }

    @Override
    public ResourceResponse updateAvatar(MultipartFile file) {
        Recruiter recruiter = getCurrentRecruiter();
        Optional<Resource> currentAvt = resourceRepository.findById(recruiter.getAvatarResourceId());
        if (currentAvt.isPresent()) {
            resourceRepository.delete(currentAvt.get());
            cloudStorageService.deleteFile(currentAvt.get().getPublicId());
        }
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(file, "avatar");
        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .resourceType(ResourceType.AVATAR)
                .url(fileInfo.url())
                .publicId(fileInfo.publicId())
                .name("avatar")
                .build();
        Resource savedResource = resourceRepository.save(resource);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public PageResult<JobResponse> getCompanyJobs(Account account, String jobStatus, int page, int size, String sortBy,
            String sortDir) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (recruiter.getCompany() == null) {
            throw new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND);
        }

        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Job> jobs = jobRepository.findJobsByCompany_IdAndStatus(recruiter.getCompany().getId(),
                JobStatus.valueOf(jobStatus), pageable);

        return PageResult.from(jobs.map(jobMapper::toResponse));
    }

    @Override
    public PageResult<JobApplicantResponse> getJobApplicants(Account account, Long jobId, int page, int size, String sortBy,
            String sortDir) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (recruiter.getCompany() == null) {
            throw new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND);
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_NOT_FOUND));

        if (!job.getCompany().getId().equals(recruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.RECRUITER_UNAUTHORIZED_ACCESS_JOB_APPLICANTS);
        }

        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<JobApplication> applications = jobApplicationRepository.findByJobId(jobId, pageable);

        return PageResult.from(applications.map(jobApplicationMapper::toApplicantResponse));
    }

    @Override
    @Transactional
    public JobApplicantResponse processCandidate(Account account, Long jobApplicationId, ApplicationStatus action) {
        JobApplication jobApplication = jobApplicationRepository.findById(jobApplicationId)
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (!jobApplication.getJob().getCompany().getId().equals(recruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.RECRUITER_UNAUTHORIZED_ACCESS_JOB_APPLICANTS);
        }

        if (jobApplication.getStatus().equals(ApplicationStatus.REJECTED)) {
            throw new AppException(ErrorCode.JOB_ALREADY_PROCESSED);
        }

        jobApplication.setStatus(action);

        return jobApplicationMapper.toApplicantResponse(jobApplicationRepository.save(jobApplication));
    }

    @Override
    public InterviewResponse scheduleInterview(Account account, CreateInterviewRequest request) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        JobApplication jobApplication = jobApplicationRepository.findById(request.getApplicationId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND));

        if (!jobApplication.getJob().getCompany().getId().equals(recruiter.getCompany().getId())) {
            throw new AppException(ErrorCode.RECRUITER_UNAUTHORIZED_ACCESS_JOB_APPLICANTS);
        }

        Interview interview = interviewRepository.save(interviewMapper.toEntity(request));

        jobApplication.setStatus(ApplicationStatus.INTERVIEW);
        jobApplicationRepository.save(jobApplication);
        emailService.sendInterviewInvitationEmail(
                interview.getLocation(),
                interview.getScheduledAt(),
                jobApplication.getCandidate().getFullName(),
                jobApplication.getCandidate().getAccount().getEmail());

        return interviewMapper.toResponse(interview);
    }

    @Override
    public InterviewResponse updateInterview(Account account, UpdateInterviewRequest request) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Interview interview = interviewRepository.findById(request.getInterviewId())
                .orElseThrow(() -> new AppException(ErrorCode.INTERVIEW_NOT_FOUND));

        OffsetDateTime oldScheduledAt = interview.getScheduledAt();

        if (!(interview.getJobApplication().getJob().getCompany().getId().equals(recruiter.getCompany().getId()))) {
            throw new AppException(ErrorCode.RECRUITER_UNAUTHORIZED_ACCESS_INTERVIEW);
        }
        interview.setScheduledAt(request.getScheduledAt());
        interview.setNotes(request.getNotes());
        interview.setStatus(request.getStatus());
        interview.setLocation(locationRepository.findById(request.getLocationId()).orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND)));

        interview = interviewRepository.save(interview);

        emailService.sendInterviewUpdateEmail(interview.getLocation(),
                oldScheduledAt,
                interview.getScheduledAt(),
                interview.getJobApplication().getCandidate().getFullName(),
                interview.getJobApplication().getCandidate().getAccount().getEmail());
        return interviewMapper.toResponse(interview);
    }

    @Override
    public PageResult<InterviewResponse> getAllInterviews(Account account, int page, int size, String sortBy, String sortDir) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Pageable pageable = PageRequest.of(page, size, Sort.by(
                sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy));

        Page<InterviewResponse> interviews = interviewRepository.findByJobApplication_Job_Company_Id(recruiter.getCompany().getId(), pageable)
                .map(interviewMapper::toResponse);

        return PageResult.from(interviews);
    }

    // Private methods
    private Recruiter getCurrentRecruiter() {
        Account account = currentAccountProvider.getCurrentAccount();
        return recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }

}
