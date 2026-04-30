package org.toanehihi.botcv.interfaces.web.dtos.interaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.toanehihi.botcv.domain.model.enums.InteractionEventType;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
public class InteractionEvent {
    @JsonProperty("job_id")
    private Long jobId;
    @JsonProperty("account_id")
    private Long accountId;
    @JsonProperty("event_type")
    private InteractionEventType eventType;
    private Map<String, Object> metadata;
    @JsonProperty("occurred_at")
    private OffsetDateTime occurredAt;
}
