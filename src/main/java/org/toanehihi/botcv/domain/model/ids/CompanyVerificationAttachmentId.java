package org.toanehihi.botcv.domain.model.ids;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CompanyVerificationAttachmentId implements Serializable {
    private Long verification;
    private Long resource;
}
