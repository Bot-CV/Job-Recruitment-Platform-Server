package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Candidate;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.SavedJob;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    void deleteByCandidateAndJob(Candidate candidate, Job job);

    Page<SavedJob> findByCandidateId(Long candidateId, Pageable pageable);

    boolean existsByCandidateAndJob(Candidate candidate, Job job);
}
