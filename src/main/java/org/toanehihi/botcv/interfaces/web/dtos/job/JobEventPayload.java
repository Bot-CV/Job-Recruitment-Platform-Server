package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Builder;
import lombok.Getter;
import org.toanehihi.botcv.domain.model.enums.JobStatus;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;
import org.toanehihi.botcv.interfaces.web.dtos.skill.SkillResponse;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Getter
public class JobEventPayload {
    private Long id;
    private String title;
    private String company;
    private String category;
    private String description;
    private SeniorityLevel seniority;
    private int minExperienceYears;
    private String location;
    private WorkMode workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private Integer maxCandidates;
    private OffsetDateTime datePosted;
    private OffsetDateTime dateExpires;
    private JobStatus status;
    private List<SkillResponse> skills;
}
