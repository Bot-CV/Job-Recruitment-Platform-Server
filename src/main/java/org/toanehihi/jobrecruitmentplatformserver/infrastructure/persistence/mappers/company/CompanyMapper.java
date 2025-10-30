package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.company;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyMapper {
    private final CompanyLocationMapper companyLocationMapper;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

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
                .email(company.getEmail())
                .phone(company.getPhone())
                .industry(company.getIndustry())
                .description(company.getDescription())
                .resource(resourceRepository.findByIdAndResourceType(company.getId(), ResourceType.COMPANY_LOGO)
                        .map(resourceMapper::toResponse)
                        .orElse(null))
                .verified(company.isVerified())
                .companyLocations(company.getCompanyLocations().stream().map(companyLocationMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }
}
