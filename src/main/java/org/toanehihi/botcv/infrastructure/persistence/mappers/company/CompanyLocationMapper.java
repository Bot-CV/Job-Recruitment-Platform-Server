package org.toanehihi.botcv.infrastructure.persistence.mappers.company;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.CompanyLocation;
import org.toanehihi.botcv.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyLocationResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CompanyLocationMapper {
    private final LocationMapper locationMapper;

    public CompanyLocationResponse toResponse(CompanyLocation companyLocation) {
        return CompanyLocationResponse.builder()
                .location(locationMapper.toResponse(companyLocation.getLocation()))
                .isHeadquarter(companyLocation.getIsHeadquarter())
                .build();
    }
}
