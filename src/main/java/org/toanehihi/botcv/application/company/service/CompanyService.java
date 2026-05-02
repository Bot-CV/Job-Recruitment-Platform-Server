package org.toanehihi.botcv.application.company.service;

import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyResponse;

public interface CompanyService {
    VerifyCompanyResponse verifyAttestation(Account account, VerifyCompanyRequest request);

    PageResult<CompanyResponse> getVerifyList(Account account, int page, int size, String sortBy, String sortDir);

    CompanyResponse getCompanyInfo(Long companyId);
}
