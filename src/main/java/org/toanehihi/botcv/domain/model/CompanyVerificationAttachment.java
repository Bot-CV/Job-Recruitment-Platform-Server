package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CompanyVerificationAttachmentId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"verification", "resource"})
@Entity
@Table(name = "company_verification_attachments")
@IdClass(CompanyVerificationAttachmentId.class)
public class CompanyVerificationAttachment {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id")
    private CompanyVerification verification;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;
}
