package org.toanehihi.botcv.interfaces.web.dtos.candidate;

import java.util.Set;

import org.toanehihi.botcv.domain.model.enums.ExperienceYears;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillRequest;

import lombok.Getter;

@Getter
public class CandidateRequest {
    private String fullName;
    private String phone;
    private String email;
    private LocationRequest location;
    private ExperienceYears experienceYears;
    private Integer salaryExpect;
    private String currency;
    private Boolean remotePref;
    private Boolean relocationPref;
    private String bio;
    private Set<CandidateSkillRequest> skills;
}
