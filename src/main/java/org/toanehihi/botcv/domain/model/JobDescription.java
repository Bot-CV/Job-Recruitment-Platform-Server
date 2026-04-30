package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_description")
public class JobDescription {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "job_id")
	private Job job;

	@Column(name = "summary", columnDefinition = "text")
	private String summary;

	@Column(name = "responsibilities", columnDefinition = "text")
	private String responsibilities;

	@Column(name = "requirements", columnDefinition = "text")
	private String requirements;

	@Column(name = "nice_to_have", columnDefinition = "text")
	private String niceToHave;

	@Column(name = "benefits", columnDefinition = "text")
	private String benefits;


	@Column(name = "hiring_process", columnDefinition = "text")
	private String hiringProcess;

	@Column(name = "notes", columnDefinition = "text")
	private String notes;
}


