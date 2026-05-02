package org.toanehihi.botcv.interfaces.web.dtos.job;


import lombok.Getter;
import org.toanehihi.botcv.domain.model.enums.EmploymentType;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class CreateJobRequest {
    private String title;
    private Long categoryId;
    private SeniorityLevel seniorityLevel;
    private EmploymentType employmentType;
    private int minExperienceYears;
    private Long locationId;
    private WorkMode workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private Integer maxCandidates;
    private OffsetDateTime dateExpires;
    private String summary;
    private String responsibilities;
    private String requirements;
    private String niceToHave;
    private String benefits;
    private String hiringProcess;
    private String notes;

    private boolean saveAsDraft;

    private List<String> skills;
}
