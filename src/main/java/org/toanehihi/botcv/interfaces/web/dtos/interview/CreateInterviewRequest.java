package org.toanehihi.botcv.interfaces.web.dtos.interview;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class CreateInterviewRequest {
    private Long applicationId;
    private OffsetDateTime scheduledAt;
    private Long locationId;
}
