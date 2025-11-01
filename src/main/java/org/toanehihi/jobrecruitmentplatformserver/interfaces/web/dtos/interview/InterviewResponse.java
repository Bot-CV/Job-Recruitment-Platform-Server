package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview;

import lombok.Builder;
import lombok.Data;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InterviewStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.location.LocationResponse;

import java.time.OffsetDateTime;

@Builder
@Data
public class InterviewResponse {
    private Long id;
    private Long applicationId;
    private String jobTitle;
    private String candidateName;
    private OffsetDateTime scheduledAt;
    private InterviewStatus status;
    private LocationResponse location;
    private String notes;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;

}
