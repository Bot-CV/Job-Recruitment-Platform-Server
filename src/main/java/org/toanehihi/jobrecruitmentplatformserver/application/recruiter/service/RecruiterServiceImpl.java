package org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service;

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
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.*;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.JobMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.recruiter.RecruiterMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.*;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyLocationRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import jakarta.transaction.Transactional;
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

    @Override
    public RecruiterResponse getProfile() {
        return recruiterMapper.toReponse(getCurrentRecruiter());
    }

    @Override
    public RecruiterResponse updateProfile(RecruiterRequest request) {
        Recruiter recruiter = getCurrentRecruiter();

        recruiter.setFullName(request.getFullName());
        Recruiter savedRecruiter = recruiterRepository.save(recruiter);
        return recruiterMapper.toReponse(savedRecruiter);
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
    public Page<JobResponse> getCompanyJobs(Account account, String jobStatus, int page, int size, String sortBy, String sortDir) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (recruiter.getCompany() == null) {
            throw new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND);
        }

        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Job> jobs = jobRepository.findJobsByCompany_IdAndStatus(recruiter.getCompany().getId(), JobStatus.valueOf(jobStatus), pageable);

        log.info(String.valueOf(jobs.getSize()));
        if(jobs.isEmpty()) {
            throw new AppException(ErrorCode.JOB_NOT_FOUND);
        }

        return jobs.map(jobMapper::toResponse);
    }

    @Override
    public Page<JobApplicantResponse> getJobApplicants(Account account, Long jobId, int page, int size, String sortBy, String sortDir) {
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

        return applications.map(jobMapper::toJobApplicantResponse);
    }

    // Private methods
    private Recruiter getCurrentRecruiter() {
        Account account = currentAccountProvider.getCurrentAccount();
        return recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTH_UNAUTHORIZED));
    }

}
