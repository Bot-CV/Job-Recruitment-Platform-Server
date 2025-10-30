package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company;

import java.time.OffsetDateTime;
import java.util.Set;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyResponse {
    private Long id;
    private String name;
    private String website;
    private String size;
    private String description;
    private String phone;
    private String email;
    private String industry;
    private ResourceResponse resource;
    private boolean verified;
    private OffsetDateTime dateCreated;
    private Set<CompanyLocationResponse> companyLocations;
}
