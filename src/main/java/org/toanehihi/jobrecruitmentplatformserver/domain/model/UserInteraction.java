package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InteractionEventType;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.converter.JsonbConverter;

import java.time.OffsetDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_interactions")
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type")
    private InteractionEventType eventType;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata;

    @Column(name = "occurred_at")
    private OffsetDateTime occurredAt;
}
