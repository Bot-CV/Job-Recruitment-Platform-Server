package org.toanehihi.botcv.application.company.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.application.email.service.EmailService;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.domain.model.Company;
import org.toanehihi.botcv.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyResponse;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public VerifyCompanyResponse verifyAttestation(Account account, VerifyCompanyRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setVerified(request.isApproved());
        Company savedCompany = companyRepository.save(company);

        company.getRecruiters().stream().findFirst().ifPresent(recruiter ->
                emailService.sendCompanyVerificationResult(
                        recruiter.getAccount().getEmail(),
                        request.isApproved(),
                        request.isApproved() ? null : request.getReason()));

        return VerifyCompanyResponse.builder()
                .companyId(savedCompany.getId())
                .approved(request.isApproved())
                .reason(request.getReason())
                .build();
    }

    @Override
    public PageResult<CompanyResponse> getVerifyList(Account account, int page, int size, String sortBy,
            String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Company> unverifiedCompaniesPage = companyRepository.findByVerifiedFalse(pageable);
        return PageResult.from(unverifiedCompaniesPage.map(companyMapper::toResponse));
    }

    @Override
    public CompanyResponse getCompanyInfo(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toResponse(company);
    }
}
