package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class JobMetadataResponse {
    private Long jobId;
    @JsonProperty("required_skills")
    private Set<String> requiredSkills;
    // etc...
}

