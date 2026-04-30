package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PopularJobResponse {
    private Long jobId;
    private Double popularityScore;
}
