package org.toanehihi.jobrecruitmentplatformserver.domain.model.ids;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AttestationResourceId implements Serializable {
    private Long company;
    private Long resource;
}
