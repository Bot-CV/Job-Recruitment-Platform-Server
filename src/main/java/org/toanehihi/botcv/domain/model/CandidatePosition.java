package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CandidatePositionId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"candidate", "category"})
@Entity
@Table(name = "candidate_positions")
@IdClass(CandidatePositionId.class)
public class CandidatePosition {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private JobCategory category;
}
