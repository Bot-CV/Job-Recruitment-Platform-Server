package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Recruiter;

import java.util.Optional;

@Repository
public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByAccountId(Long accountId);
}


