package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.company;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service.RecruiterService;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {
    private final RecruiterService recruiterService;

    @GetMapping("/public/{companyId}")
    DataResponse<CompanyResponse> getCompanyProfile(@PathVariable Long companyId) {
        return DataResponse.<CompanyResponse>builder()
                .data(recruiterService.getCompany(companyId))
                .build();
    }

    @PutMapping
    DataResponse<CompanyResponse> updateCompany(@RequestBody CompanyRequest request) {
        return DataResponse.<CompanyResponse>builder()
                .data(recruiterService.updateCompany(request))
                .build();
    }
}
