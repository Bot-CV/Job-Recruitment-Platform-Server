package org.toanehihi.botcv.interfaces.web.dtos.skill;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class CandidateSkillRequest {
    private String skillName;
    @Min(value = 0, message = "LEVEL_INVALID")
    @Max(value = 5, message = "LEVEL_INVALID")
    private Integer level;
}
