package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Getter;
import java.util.Map;

@Getter
public class JobSearchRequest {
    private String query;
    private int limit;
    private int offset;
    private Map<String, String> filters;
}
