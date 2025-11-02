package org.toanehihi.jobrecruitmentplatformserver.application.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.*;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AttestationResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.HasRecruiterRole;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import jakarta.transaction.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongConsumer;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final ResourceRepository resourceRepository;
    private final AttestationResourceRepository attestationResourceRepository;
    private final CloudStorageService cloudStorageService;
    private final ResourceMapper resourceMapper;
    private final CompanyRepository companyRepository;

    @Override
    public ResourceResponse updateUserAvatar(Account account, MultipartFile avatar) {
        String roleName = account.getRole().getName();

        return switch (roleName) {
            case "RECRUITER" -> {
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
            case "CANDIDATE" -> {
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
                .findByIdAndResourceType(currentAvatarId, ResourceType.AVATAR);

        currentAvatar.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });

        // Upload and save new avatar
        CloudinaryStorageImpl.CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(avatar, "avatar");

        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.AVATAR)
                .url(fileInfo.url())
                .publicId(fileInfo.publicId())
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
                .findByIdAndResourceType(company.getLogoResourceId(), ResourceType.COMPANY_LOGO);

        currentLogo.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });
        CloudinaryStorageImpl.CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(logo, "company_logo");
        Resource resource = Resource.builder()
                .mimeType(fileInfo.mimeType())
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.COMPANY_LOGO)
                .url(fileInfo.url())
                .publicId(fileInfo.publicId())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        company.setLogoResourceId(savedResource.getId());
        companyRepository.save(company);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public ResourceResponse uploadJobAttachment(MultipartFile file) {
        return null;
    }

    @Override
    @HasRecruiterRole
    @Transactional
    public List<ResourceResponse> uploadAttestation(Account account, List<MultipartFile> files) {
        if (!account.getRole().getName().equals("RECRUITER")) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }

        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        if (recruiter.getCompany().isVerified()) {
            throw new AppException(ErrorCode.COMPANY_HAS_BEEN_VERIFIED);
        }

        if (attestationResourceRepository.existsByCompany(recruiter.getCompany())) {
            throw new AppException(ErrorCode.RESOURCE_ATTESTATION_HAS_BEEN_SENT);
        }
        Company company = recruiter.getCompany();
        Set<AttestationResource> attestations = new HashSet<>();
        for (MultipartFile file : files) {
            CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(file, "attestation");
            Resource resource = Resource.builder()
                    .mimeType(fileInfo.mimeType())
                    .contentType(fileInfo.contentType())
                    .resourceType(ResourceType.ATTESTATION)
                    .url(fileInfo.url())
                    .publicId(fileInfo.publicId())
                    .name(fileInfo.fileName())
                    .build();
            Resource savedResource = resourceRepository.save(resource);
            AttestationResource attestation = AttestationResource.builder()
                    .company(company)
                    .resource(savedResource)
                    .build();
            attestations.add(attestation);
        }
        company.getAttestations().addAll(attestations);
        companyRepository.save(company);
        return attestations.stream()
                .map(attestation -> resourceMapper.toResponse(attestation.getResource()))
                .toList();
    }
}
