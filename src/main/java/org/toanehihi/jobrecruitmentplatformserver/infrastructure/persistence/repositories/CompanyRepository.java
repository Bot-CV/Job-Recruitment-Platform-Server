package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByRecruiter(Recruiter recruiter);

    @Query(value = """
            SELECT c
            FROM Company c
            LEFT JOIN AttestationResource ar ON ar.company = c
            WHERE c.verified = false
            AND ar.id IS NOT NULL
            """)
    Page<Company> findAllUnverifiedCompanies(Pageable pageable);

    @Query(value = """
            SELECT COUNT(c)
            FROM Company c
            WHERE c.verified = false
            AND EXISTS (
                SELECT 1
                FROM AttestationResource a
                WHERE a.company = c
                )
            """)
    Long countUnverifiedCompanies();
}
