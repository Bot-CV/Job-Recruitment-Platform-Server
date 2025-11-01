package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.company;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.toanehihi.jobrecruitmentplatformserver.application.company.service.CompanyService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.VerifyCompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("/{companyId}")
    DataResponse<CompanyResponse> getCompanyInfo(@PathVariable Long companyId) {
        return DataResponse.<CompanyResponse>builder()
                .data(companyService.getCompanyInfo(companyId))
                .build();
    }

    @GetMapping("/verify")
    DataResponse<PageResult<CompanyResponse>> getVerifyList(@CurrentUser Account account, int page, int size,
            String sortBy,
            String sortDir) {
        return DataResponse.<PageResult<CompanyResponse>>builder()
                .data(companyService.getVerifyList(account, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{companyId}/attestations")
    DataResponse<List<ResourceResponse>> getCompanyAttestations(@CurrentUser Account account,
            @PathVariable Long companyId) {
        return DataResponse.<List<ResourceResponse>>builder()
                .data(companyService.getCompanyAttestations(account, companyId))
                .build();
    }

    @PatchMapping("/verify")
    DataResponse<VerifyCompanyResponse> verifyAttestation(@CurrentUser Account account,
            @RequestBody VerifyCompanyRequest request) {
        return DataResponse.<VerifyCompanyResponse>builder()
                .data(companyService.verifyAttestation(account, request))
                .build();
    }
}
