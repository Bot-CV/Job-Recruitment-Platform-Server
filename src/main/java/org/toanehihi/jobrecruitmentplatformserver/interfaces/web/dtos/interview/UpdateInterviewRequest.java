package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview;

import lombok.Data;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InterviewStatus;

import java.time.OffsetDateTime;

@Data
public class UpdateInterviewRequest {
    private Long interviewId;
    private OffsetDateTime scheduledAt;
    private InterviewStatus status;
    private String notes;
    private Long locationId;
}
