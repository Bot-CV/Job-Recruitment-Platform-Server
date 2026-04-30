package org.toanehihi.botcv.interfaces.web.dtos.company;

import org.toanehihi.botcv.interfaces.web.dtos.location.LocationRequest;

import lombok.Getter;

@Getter
public class CompanyLocationRequest {
    private LocationRequest location;
    private Boolean isHeadquarter;
}
