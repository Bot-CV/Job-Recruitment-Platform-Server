package org.toanehihi.botcv.interfaces.web.dtos.company;

import org.toanehihi.botcv.interfaces.web.dtos.location.LocationResponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyLocationResponse {
    private LocationResponse location;
    private Boolean isHeadquarter;
}
