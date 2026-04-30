package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.InterviewStatus;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplication jobApplication;

    private OffsetDateTime scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
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
