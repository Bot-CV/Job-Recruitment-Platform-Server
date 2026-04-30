package org.toanehihi.botcv.domain.model.ids;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CandidateSkillId implements Serializable {
	private Long candidate;
	private Long skill;
}


