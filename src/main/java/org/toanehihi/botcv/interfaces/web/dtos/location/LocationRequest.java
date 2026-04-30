package org.toanehihi.botcv.interfaces.web.dtos.location;

import lombok.Getter;

@Getter
public class LocationRequest {
    private String streetAddress;
    private String ward;
    private String district;
    private String provinceCity;
    private String country;
}
