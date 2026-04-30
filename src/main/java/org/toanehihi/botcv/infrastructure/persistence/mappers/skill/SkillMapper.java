package org.toanehihi.botcv.infrastructure.persistence.mappers.skill;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Skill;
import org.toanehihi.botcv.interfaces.web.dtos.skill.SkillResponse;

@Component
public class SkillMapper {
    public SkillResponse toResponse(Skill skill) {
        return SkillResponse.builder()
                .id(skill.getId())
                .name(skill.getName())
                .aliases(skill.getAliases())
                .dateCreated(skill.getDateCreated())
                .build();
    }
}
