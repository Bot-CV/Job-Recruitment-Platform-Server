package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationItem {
    @JsonProperty("job_id")
    private Long jobId;
    private Double popularityScore;
}

