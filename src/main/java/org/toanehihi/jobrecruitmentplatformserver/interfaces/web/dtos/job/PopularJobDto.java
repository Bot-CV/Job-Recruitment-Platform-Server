package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import lombok.Getter;
import lombok.Setter;

public interface PopularJobDto {
    Long getJobId();
    Double getPopularityScore();
}
