package org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.CompanyLocation;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Location;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.recruiter.RecruiterMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.LocationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.security.CurrentAccountProvider;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyLocationRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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

    // Private methods
    private Recruiter getCurrentRecruiter() {
        Account account = currentAccountProvider.getCurrentAccount();
        return recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_NOT_FOUND));
    }

}
