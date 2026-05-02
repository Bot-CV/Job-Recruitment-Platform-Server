package org.toanehihi.botcv.infrastructure.persistence.mappers.skill;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.CandidateSkill;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidateSkillMapper {
    private final SkillMapper skillMapper;

    public CandidateSkillResponse toResponse(CandidateSkill candidateSkill) {
        return CandidateSkillResponse.builder()
                .skill(skillMapper.toResponse(candidateSkill.getSkill()))
                .build();
    }
}
