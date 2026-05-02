package org.toanehihi.botcv.interfaces.web.dtos.candidate;

import java.time.OffsetDateTime;
import java.util.Set;

import org.toanehihi.botcv.domain.model.enums.ExperienceYears;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillResponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateResponse {
    private Long id;
    private Long accountId;
    private String fullName;
    private String phone;
    private String email;
    private LocationResponse location;
    private ExperienceYears experienceYears;
    private Integer salaryExpect;
    private String currency;
    private Boolean remotePref;
    private Boolean relocationPref;
    private ResourceResponse resource;
    private String bio;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
    private Set<CandidateSkillResponse> skills;
}
