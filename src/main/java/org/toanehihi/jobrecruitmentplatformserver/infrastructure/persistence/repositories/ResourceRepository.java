package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Resource;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ResourceType;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Optional<Resource> findByIdAndResourceType(Long id, ResourceType resourceType);

    Optional<Resource> findByPublicId(String publicId);

    List<Resource> findAllByOwnerIdAndResourceType(Long ownerId, ResourceType resourceType);

    Page<Resource> findByOwnerIdAndResourceType(Long ownerId, ResourceType resourceType, Pageable pageable);
}
