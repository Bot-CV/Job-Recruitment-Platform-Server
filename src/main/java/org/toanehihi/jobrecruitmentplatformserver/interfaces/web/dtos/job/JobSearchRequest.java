package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
public class JobSearchRequest {
    private String query;
    private int limit;
    private int offset;
    private Map<String, String> filters;
}
