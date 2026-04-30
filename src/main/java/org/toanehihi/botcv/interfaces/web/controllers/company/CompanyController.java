package org.toanehihi.botcv.interfaces.web.controllers.company;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.toanehihi.botcv.application.company.service.CompanyService;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.interfaces.annotation.CurrentUser;
import org.toanehihi.botcv.interfaces.annotation.HasAdminRole;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("/public/{companyId}")
    DataResponse<CompanyResponse> getCompanyInfo(@PathVariable Long companyId) {
        return DataResponse.<CompanyResponse>builder()
                .data(companyService.getCompanyInfo(companyId))
                .build();
    }

    @GetMapping("/verify")
    @HasAdminRole
    DataResponse<PageResult<CompanyResponse>> getVerifyList(
            @CurrentUser Account account,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateUpdated") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return DataResponse.<PageResult<CompanyResponse>>builder()
                .data(companyService.getVerifyList(account, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/{companyId}/attestations")
    @HasAdminRole
    DataResponse<List<ResourceResponse>> getCompanyAttestations(@CurrentUser Account account,
            @PathVariable Long companyId) {
        return DataResponse.<List<ResourceResponse>>builder()
                .data(companyService.getCompanyAttestations(account, companyId))
                .build();
    }

    @PatchMapping("/verify")
    @HasAdminRole
    DataResponse<VerifyCompanyResponse> verifyAttestation(@CurrentUser Account account,
            @RequestBody VerifyCompanyRequest request) {
        return DataResponse.<VerifyCompanyResponse>builder()
                .data(companyService.verifyAttestation(account, request))
                .build();
    }
}
