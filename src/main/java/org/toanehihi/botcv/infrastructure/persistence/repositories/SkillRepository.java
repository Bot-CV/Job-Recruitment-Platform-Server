package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Skill;

import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
	Optional<Skill> findByName(String name);

	Optional<Skill> findByNameIgnoreCase(String name);

	boolean existsByName(String name);
}
