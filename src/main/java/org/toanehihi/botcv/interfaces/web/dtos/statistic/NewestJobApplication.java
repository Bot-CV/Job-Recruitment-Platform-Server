package org.toanehihi.botcv.interfaces.web.dtos.statistic;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Builder
@Data
public class NewestJobApplication {
    private String jobTitle;
    private String candidateName;
    private OffsetDateTime appliedAt;
}
