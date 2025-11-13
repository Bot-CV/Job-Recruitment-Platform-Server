package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @NonNull
    Page<Job> findAll(@NonNull Pageable pageable);

    Page<Job> findJobsByCompany_IdAndStatus(@NonNull Long companyId, @NonNull JobStatus jobStatus,
            Pageable pageable);

    Long countByStatus(JobStatus status);

    @Query(value = """
            SELECT COUNT(j)
            FROM Job j
            WHERE j.datePosted >= :startDate
            AND j.datePosted < :endDate
            AND j.status = :status
            """)
    Long countJobCreatedBetweenWithStatus(
            @Param("startDate") OffsetDateTime startDate,
            @Param("endDate") OffsetDateTime endDate,
            @Param("status") JobStatus status);

    Long countByCompany_IdAndStatus(@NonNull Long companyId, @NonNull JobStatus jobStatus);

    @Query(value = """
            SELECT * FROM jobs
            WHERE jobs.company_id = ?1
            ORDER BY jobs.date_posted DESC
            LIMIT 3
            """, nativeQuery = true)
    List<Job> findNewestJob(Long companyId);

    @Query("select j from Job j\n" +
            " left join fetch j.skills s\n" +
            " left join fetch j.jobRole jr\n" +
            " left join fetch j.location l\n" +
            " left join fetch j.company c\n" +
            " where j.id = :id")
    Optional<Job> findByIdWithRelations(@NonNull Long id);
}
