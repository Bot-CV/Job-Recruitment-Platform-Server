package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationData {
    private Integer count;
    private List<JobRecommendationItem> recommendations;

    @JsonProperty("user_id")
    private Long userId;
}

