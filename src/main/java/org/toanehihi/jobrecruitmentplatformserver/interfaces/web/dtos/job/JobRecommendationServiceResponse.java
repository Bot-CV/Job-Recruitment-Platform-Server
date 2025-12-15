package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationServiceResponse {
    private Integer code;
    private JobRecommendationData data;
}

