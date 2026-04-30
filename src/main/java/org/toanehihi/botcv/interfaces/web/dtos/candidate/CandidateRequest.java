package org.toanehihi.botcv.interfaces.web.dtos.candidate;

import java.util.Set;

import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillRequest;

import lombok.Getter;

@Getter
public class CandidateRequest {
    private String fullName;
    private String phone;
    private String email;
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
