package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.EventType;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.converter.JsonbConverter;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "analytics")
public class Analytic {
    @Id
    private Long id;
    private Long accountId;
    private Long targetId;
    private EventType eventType;
    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata;
    private OffsetDateTime occurredAt;
}
