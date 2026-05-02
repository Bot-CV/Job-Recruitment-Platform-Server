package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidatePosition;
import org.toanehihi.botcv.domain.model.ids.CandidatePositionId;

import java.util.List;

@Repository
public interface CandidatePositionRepository extends JpaRepository<CandidatePosition, CandidatePositionId> {
    List<CandidatePosition> findByCandidateId(Long candidateId);
}
