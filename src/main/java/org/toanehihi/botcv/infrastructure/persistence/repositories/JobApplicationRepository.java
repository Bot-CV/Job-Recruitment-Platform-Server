package org.toanehihi.botcv.infrastructure.persistence.repositories;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Candidate;
import org.toanehihi.botcv.domain.model.JobApplication;
import org.toanehihi.botcv.domain.model.enums.ApplicationStatus;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsJobApplicationByJobId(Long jobId);

    @EntityGraph(value = "JobApplication.withCompany")
    Optional<JobApplication> findWithDetailsById(Long id);

    Page<JobApplication> findByCandidate(Candidate candidate, Pageable pageable);

    Page<JobApplication> findByJobId(Long jobId, Pageable pageable);

    Long countByJob_Company_IdAndStatus(Long companyId, ApplicationStatus status);

    Long countByJob_Company_IdAndAppliedAtBetween(Long jobId, OffsetDateTime startDate, OffsetDateTime endDate);


    Optional<List<JobApplication>> findTop3ByJobCompanyIdOrderByAppliedAtDesc(Long companyId);

    Optional<JobApplication> findFirstByCvResourceId(Long cvResourceId);
}
