package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import lombok.Getter;
import lombok.Setter;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageSearchResult;

import java.util.List;

@Getter
@Setter
public class JobSearchServiceResponse {
    List<Long> jobIds;
    PageSearchResult pagination;
}
