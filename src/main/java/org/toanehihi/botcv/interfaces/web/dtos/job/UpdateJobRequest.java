package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class UpdateJobRequest {
    private String title;
    private Long jobRoleId;
    private String seniorityLevel;
    private String employmentType;
    private Integer minExperienceYears;
    private Long locationId;
    private String workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private OffsetDateTime dateExpires;
    private String summary;
    private String responsibilities;
    private String requirements;
    private String niceToHave;
    private String benefits;
    private String hiringProcess;
    private String notes;
    private List<String> skills;
}
