package org.toanehihi.botcv.application.resource.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.candidate.service.CandidateService;
import org.toanehihi.botcv.application.cloud.service.CloudStorageService;
import org.toanehihi.botcv.application.cloud.service.CloudinaryStorageImpl;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.*;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.domain.model.enums.RoleName;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.interfaces.web.dtos.resource.FileData;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResumeAnalysisResponse;

import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.LongConsumer;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final ResourceRepository resourceRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final ResourceMapper resourceMapper;
    private final CloudStorageService cloudStorageService;
    private final CandidateService candidateService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ner-service-url}")
    private String nerServiceUrl;

    @Value("${app.cloudinary.base-url:}")
    private String cloudinaryBaseUrl;

    @Override
    public ResourceResponse updateUserAvatar(Account account, MultipartFile avatar) {
        RoleName role = RoleName.valueOf(account.getRole().getName());

        return switch (role) {
            case RECRUITER -> {
                Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));
                yield updateAvatar(
                        recruiter.getAvatarResourceId(),
                        avatar,
                        resourceId -> {
                            recruiter.setAvatarResourceId(resourceId);
                            recruiter.setDateUpdated(OffsetDateTime.now());
                            recruiterRepository.save(recruiter);
                        });
            }
            case CANDIDATE -> {
                Candidate candidate = candidateRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));
                yield updateAvatar(
                        candidate.getAvatarResourceId(),
                        avatar,
                        resourceId -> {
                            candidate.setAvatarResourceId(resourceId);
                            candidate.setDateUpdated(OffsetDateTime.now());
                            candidateRepository.save(candidate);
                        });
            }
            default -> throw new AppException(ErrorCode.ACCOUNT_DOES_NOT_SUPPORT);
        };
    }

    private ResourceResponse updateAvatar(
            Long currentAvatarId,
            MultipartFile avatar,
            LongConsumer updateEntity) {

        // Delete existing avatar if present
        Optional<Resource> currentAvatar = resourceRepository
                .findByIdAndResourceType(currentAvatarId, ResourceType.IMAGE);

        currentAvatar.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });

        // Upload and save new avatar
        CloudinaryStorageImpl.CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(avatar, "avatar");

        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.IMAGE)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();

        Resource savedResource = resourceRepository.save(resource);

        // Update entity with new avatar ID
        updateEntity.accept(savedResource.getId());

        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public ResourceResponse updateCompanyLogo(Account account, MultipartFile logo) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Company company = recruiter.getCompany();
        if (company == null) {
            throw new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND);
        }

        Optional<Resource> currentLogo = resourceRepository
                .findByIdAndResourceType(company.getLogoResourceId(), ResourceType.IMAGE);

        currentLogo.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });
        CloudinaryStorageImpl.CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(logo, "company_logo");
        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.IMAGE)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        company.setLogoResourceId(savedResource.getId());
        companyRepository.save(company);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public ResourceResponse uploadResume(Account account, MultipartFile file) {
        candidateRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));
        CloudinaryStorageImpl.CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(file, "resume");
        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.DOCUMENT)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse analyzeResume(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (resource.getResourceType() != ResourceType.DOCUMENT) {
            throw new AppException(ErrorCode.RESOURCE_TYPE_NOT_ALLOWED);
        }
        String resourceUrl = buildResourceUrl(resource.getPublicId());
        FileData fileData = cloudStorageService.downloadFile(resourceUrl);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileData.getContent()) {
            @Override
            public String getFilename() {
                return resource.getName();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<NerExtractResponse> response = restTemplate.postForEntity(
                nerServiceUrl + "/extract",
                requestEntity,
                NerExtractResponse.class);

        NerExtractResponse wrapper = response.getBody();
        ResumeAnalysisResponse analysisResult = (wrapper != null) ? wrapper.entities : null;

        if (analysisResult != null) {
            jobApplicationRepository.findFirstByCvResourceId(resourceId)
                    .map(JobApplication::getCandidate)
                    .ifPresent(candidate ->
                            candidateService.updateProfileFromCV(candidate.getAccount().getId(), analysisResult));
        }
        return analysisResult;
    }

    private String buildResourceUrl(String publicId) {
        if (publicId == null) return null;
        if (cloudinaryBaseUrl != null && !cloudinaryBaseUrl.isBlank()) {
            return cloudinaryBaseUrl + "/" + publicId;
        }
        return publicId;
    }

    public record NerExtractResponse(ResumeAnalysisResponse entities) {
    }
}
