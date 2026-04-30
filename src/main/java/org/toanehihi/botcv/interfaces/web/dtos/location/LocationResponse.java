package org.toanehihi.botcv.interfaces.web.dtos.location;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LocationResponse {
    private Long id;
    private String streetAddress;
    private String ward;
    private String district;
    private String provinceCity;
    private String country;
    private BigDecimal lat;
    private BigDecimal lng;
}
