package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate;

import java.util.Set;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.SeniorityLevel;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.location.LocationRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.skill.CandidateSkillRequest;

import lombok.Getter;

@Getter
public class CandidateRequest {
    private String fullName;
    private String phone;
    private LocationRequest location;
    private SeniorityLevel seniority;
    private Integer salaryExpectMin;
    private Integer salaryExpectMax;
    private String currency;
    private Boolean remotePref;
    private Boolean relocationPref;
    private String bio;
    private Set<CandidateSkillRequest> skills;
}
