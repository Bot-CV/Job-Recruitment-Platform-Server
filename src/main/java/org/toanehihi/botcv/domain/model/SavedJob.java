package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "saved_jobs")
public class SavedJob {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id")
	private Job job;

	@CreationTimestamp
	@Column(name = "saved_at")
	private OffsetDateTime savedAt;
}


