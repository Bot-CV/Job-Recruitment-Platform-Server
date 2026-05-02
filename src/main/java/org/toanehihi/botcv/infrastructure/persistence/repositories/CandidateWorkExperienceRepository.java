package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidateWorkExperience;

import java.util.List;

@Repository
public interface CandidateWorkExperienceRepository extends JpaRepository<CandidateWorkExperience, Long> {
    List<CandidateWorkExperience> findByCandidateId(Long candidateId);
}
