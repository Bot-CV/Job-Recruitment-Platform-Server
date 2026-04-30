package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.JobRole;

@Repository
public interface JobRoleRepository extends JpaRepository<JobRole, Long> {
}


