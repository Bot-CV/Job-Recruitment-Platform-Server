package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InteractionEventType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.converter.JsonbConverter;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_interactions")
public class UserInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long accountId;
    private Long jobId;

    @Enumerated(EnumType.STRING)
    private InteractionEventType eventType;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata;

    private OffsetDateTime occurredAt;
}
