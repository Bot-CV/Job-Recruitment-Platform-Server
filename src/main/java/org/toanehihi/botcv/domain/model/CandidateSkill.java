package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CandidateSkillId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"candidate", "skill"})
@Entity
@Table(name = "candidate_skills")
@IdClass(CandidateSkillId.class)
public class CandidateSkill {

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "candidate_id")
	private Candidate candidate;

	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "skill_id")
	private Skill skill;

	@Column(name = "level")
	private Integer level;
}


