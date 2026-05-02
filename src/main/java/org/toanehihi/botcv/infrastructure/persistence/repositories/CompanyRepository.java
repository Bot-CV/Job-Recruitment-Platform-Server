package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Page<Company> findByVerifiedFalse(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.verified = false")
    Long countUnverifiedCompanies();
}
