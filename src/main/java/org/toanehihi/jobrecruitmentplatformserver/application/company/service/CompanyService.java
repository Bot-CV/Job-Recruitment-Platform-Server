package org.toanehihi.jobrecruitmentplatformserver.application.company.service;

import java.util.List;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

public interface CompanyService {
    VerifyCompanyResponse verifyAttestation(Account account, VerifyCompanyRequest request);

    PageResult<CompanyResponse> getVerifyList(Account account, int page, int size, String sortBy, String sortDir);

    CompanyResponse getCompanyInfo(Long companyId);

    List<ResourceResponse> getCompanyAttestations(Account account, Long companyId);
}
