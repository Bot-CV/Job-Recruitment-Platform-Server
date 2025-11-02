package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.AttestationResource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Company;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.ids.AttestationResourceId;

@Repository
public interface AttestationResourceRepository extends JpaRepository<AttestationResource, AttestationResourceId> {
    List<AttestationResource> findByCompany(Company company);

    boolean existsByCompany(Company company);
}
