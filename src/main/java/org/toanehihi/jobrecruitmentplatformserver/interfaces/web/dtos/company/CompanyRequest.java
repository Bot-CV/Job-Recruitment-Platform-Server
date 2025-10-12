package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company;

import java.util.List;
import lombok.Getter;

@Getter
public class CompanyRequest {
    private String name;
    private String website;
    private String size;
    private List<CompanyLocationRequest> companyLocations;
}
