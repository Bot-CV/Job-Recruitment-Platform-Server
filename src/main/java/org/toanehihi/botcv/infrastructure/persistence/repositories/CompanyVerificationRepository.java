package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CompanyVerification;
import org.toanehihi.botcv.domain.model.enums.VerificationStatus;

@Repository
public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {
    Page<CompanyVerification> findByStatus(VerificationStatus status, Pageable pageable);
    Page<CompanyVerification> findByCompanyId(Long companyId, Pageable pageable);
}
