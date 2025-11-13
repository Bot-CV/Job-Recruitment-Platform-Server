package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate;

import lombok.Builder;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
@Builder
public class UserProfileBasedResponse {
    private Long id;
    private Set<String> skills;
    private Set<String> educations;
    private Set<String> location;
    private Map<String, Boolean> preferences;
}
