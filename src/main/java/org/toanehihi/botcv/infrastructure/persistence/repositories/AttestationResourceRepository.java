package org.toanehihi.botcv.infrastructure.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.AttestationResource;
import org.toanehihi.botcv.domain.model.Company;
import org.toanehihi.botcv.domain.model.ids.AttestationResourceId;

@Repository
public interface AttestationResourceRepository extends JpaRepository<AttestationResource, AttestationResourceId> {
    List<AttestationResource> findByCompany(Company company);

    boolean existsByCompany(Company company);
}
