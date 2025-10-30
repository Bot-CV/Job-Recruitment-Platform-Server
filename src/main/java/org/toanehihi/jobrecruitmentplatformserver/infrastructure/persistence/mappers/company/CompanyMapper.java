package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyMapper {
    private final CompanyLocationMapper companyLocationMapper;

    public void updateCompany(Company company, CompanyRequest request) {
        company.setName(request.getName());
        company.setWebsite(request.getWebsite());
        company.setSize(request.getWebsite());
        company.setDescription(request.getDescription());
        company.setPhone(request.getPhone());
        company.setEmail(request.getEmail());
        company.setIndustry(request.getIndustry());
    }

    public CompanyResponse toResponse(Company company) {
        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .website(company.getWebsite())
                .size(company.getSize())
                .logoResourceId(company.getLogoResourceId())
                .verified(company.isVerified())
                .companyLocations(company.getCompanyLocations().stream().map(companyLocationMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }
}
