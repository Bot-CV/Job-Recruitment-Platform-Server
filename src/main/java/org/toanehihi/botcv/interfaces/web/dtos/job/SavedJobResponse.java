package org.toanehihi.botcv.interfaces.web.dtos.job;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SavedJobResponse {
    private Long id;
    private JobResponse job;
    private OffsetDateTime savedAt;
}
