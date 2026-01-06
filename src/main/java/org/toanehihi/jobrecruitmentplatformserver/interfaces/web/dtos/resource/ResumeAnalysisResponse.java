package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResumeAnalysisResponse {
    @JsonProperty("NAME")
    private List<String> name;

    @JsonProperty("EMAIL ADDRESS")
    private List<String> email;

    @JsonProperty("SKILLS")
    private List<String> skills;

    @JsonProperty("YEARS OF EXPERIENCE")
    private List<String> yearsOfExperience;

    @JsonProperty("COMPANIES WORKED AT")
    private List<String> companies;

    @JsonProperty("WORKED AS")
    private List<String> jobTitles;

    @JsonProperty("UNIVERSITY")
    private List<String> universities;

    @JsonProperty("LINKEDIN LINK")
    private List<String> linkedin;

    @JsonProperty("LOCATION")
    private List<String> location;

    public String getPrimaryName() {
        return (name != null && !name.isEmpty()) ? name.get(0) : null;
    }

    public int getParsedYearsOfExperience() {
        if (yearsOfExperience == null || yearsOfExperience.isEmpty())
            return 0;
        try {
            String yearStr = yearsOfExperience.get(0).replaceAll("\\D", "");
            return yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
        } catch (Exception e) {
            return 0;
        }
    }
}
