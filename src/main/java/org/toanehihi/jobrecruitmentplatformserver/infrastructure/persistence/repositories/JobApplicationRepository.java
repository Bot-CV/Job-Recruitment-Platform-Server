package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobApplication;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsJobApplicationByJobId(Long jobId);

    @EntityGraph(value = "JobApplication.withCompany")
    Optional<JobApplication> findWithDetailsById(Long id);
}
