package org.toanehihi.jobrecruitmentplatformserver.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InterviewStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private JobApplication jobApplication;

    private OffsetDateTime scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    private String notes;

    @Column(name = "date_created", columnDefinition = "TIMESTAMP WITH TIME ZONE", updatable = false)
    @CreationTimestamp
    private OffsetDateTime dateCreated;

    @Column(name = "date_updated")
    @UpdateTimestamp
    private OffsetDateTime dateUpdated;


}
