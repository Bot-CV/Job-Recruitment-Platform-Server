package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageSearchResult;

import java.util.List;

@Getter
@Setter
@Builder
public class JobSearchResponse {
    List<JobResponse> jobs;
    PageSearchResult pagination;
}
