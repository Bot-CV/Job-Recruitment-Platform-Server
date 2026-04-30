package org.toanehihi.botcv.interfaces.web.dtos.skill;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SkillResponse {
    private Long id;
    private String name;
    private String aliases;
    private OffsetDateTime dateCreated;
}
