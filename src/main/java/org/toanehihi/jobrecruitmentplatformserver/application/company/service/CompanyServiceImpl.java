package org.toanehihi.jobrecruitmentplatformserver.application.company.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.email.service.EmailService;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.AttestationResource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AttestationResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.HasAdminRole;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final AttestationResourceRepository attestationResourceRepository;
    private final ResourceMapper resourceMapper;
    private final CompanyMapper companyMapper;
    private final CloudStorageService cloudStorageService;
    private final EmailService emailService;

    private static final String ADMIN = "ADMIN";

    @Override
    @HasAdminRole
    @Transactional
    public VerifyCompanyResponse verifyAttestation(Account account, VerifyCompanyRequest request) {
        if (!account.getRole().getName().equals(ADMIN)) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        if (!request.isApproved()) {
            List<AttestationResource> attesations = attestationResourceRepository.findByCompany(company);
            for (AttestationResource attestation : attesations) {
                Resource resource = attestation.getResource();
                cloudStorageService.deleteFile(resource.getPublicId());
            }
            company.getAttestations().clear();
            emailService.sendCompanyVerificationResult(company.getRecruiter().getAccount().getEmail(),
                    request.isApproved(), request.getReason());
        } else {
            emailService.sendCompanyVerificationResult(company.getRecruiter().getAccount().getEmail(),
                    request.isApproved(), null);
        }
        company.setVerified(request.isApproved());
        Company savedCompany = companyRepository.save(company);
        return VerifyCompanyResponse.builder()
                .companyId(savedCompany.getId())
                .approved(request.isApproved())
                .reason(request.getReason())
                .build();
    }

    @Override
    @HasAdminRole
    public PageResult<CompanyResponse> getVerifyList(Account account, int page, int size, String sortBy,
            String sortDir) {
        if (!account.getRole().getName().equals(ADMIN)) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Company> unverifiedCompaniesPage = companyRepository.findAllUnverifiedCompanies(pageable);
        return PageResult.from(unverifiedCompaniesPage.map(companyMapper::toResponse));

    }

    @Override
    public CompanyResponse getCompanyInfo(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toResponse(company);
    }

    @Override
    @HasAdminRole
    public List<ResourceResponse> getCompanyAttestations(Account account, Long companyId) {
        if (!account.getRole().getName().equals(ADMIN)) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        List<AttestationResource> attestations = attestationResourceRepository.findByCompany(company);
        return attestations.stream()
                .map(ar -> resourceMapper.toResponse(ar.getResource()))
                .toList();
    }

}
