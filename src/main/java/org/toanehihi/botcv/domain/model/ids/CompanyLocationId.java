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
public class CompanyLocationId implements Serializable {
	private Long company;
	private Long location;
}


