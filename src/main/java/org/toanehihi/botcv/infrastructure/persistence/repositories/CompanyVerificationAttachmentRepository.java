package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CompanyVerificationAttachment;
import org.toanehihi.botcv.domain.model.ids.CompanyVerificationAttachmentId;

import java.util.List;

@Repository
public interface CompanyVerificationAttachmentRepository extends JpaRepository<CompanyVerificationAttachment, CompanyVerificationAttachmentId> {
    List<CompanyVerificationAttachment> findByVerificationId(Long verificationId);
}
