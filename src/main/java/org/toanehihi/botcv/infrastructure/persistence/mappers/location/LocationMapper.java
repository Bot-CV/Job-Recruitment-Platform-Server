package org.toanehihi.botcv.infrastructure.persistence.mappers.location;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Location;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationResponse;

@Component
public class LocationMapper {
    public Location toLocation(LocationRequest request) {
        return Location.builder()
                .streetAddress(request.getStreetAddress())
                .ward(request.getWard())
                .district(request.getDistrict())
                .provinceCity(request.getProvinceCity())
                .country(request.getCountry())
                .build();
    }

    public void updateLocation(Location location, LocationRequest request) {
        location.setStreetAddress(request.getStreetAddress());
        location.setWard(request.getWard());
        location.setDistrict(request.getDistrict());
        location.setProvinceCity(request.getProvinceCity());
        location.setCountry(request.getCountry());
    }

    public LocationResponse toResponse(Location location) {
        return LocationResponse.builder()
                .id(location.getId())
                .streetAddress(location.getStreetAddress())
                .ward(location.getWard())
                .district(location.getDistrict())
                .provinceCity(location.getProvinceCity())
                .country(location.getCountry())
                .build();
    }
}
