package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.OutboxStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@Table(name = "outbox_events")
@Builder
@NoArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String aggregateType;   // "JOB"

    @Column(nullable = false)
    private Long aggregateId;       // job_id

    @Column(nullable = false, length = 20)
    private String eventType;       // CREATED/UPDATED/DELETED

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;         // JSON string

    @Column(nullable = false)
    @Builder.Default
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(nullable = false, columnDefinition = "uuid default gen_random_uuid()")
    private UUID traceId;
}

