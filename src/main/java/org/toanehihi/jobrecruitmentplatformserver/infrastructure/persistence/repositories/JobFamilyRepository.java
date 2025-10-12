package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobFamily;

import java.util.Optional;

@Repository
public interface JobFamilyRepository extends JpaRepository<JobFamily, Long> {
	Page<JobFamily> findAll(Pageable pageable);
}


