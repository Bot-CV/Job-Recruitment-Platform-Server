package org.toanehihi.botcv.interfaces.web.dtos.skill;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateSkillResponse {
    private SkillResponse skill;
}
