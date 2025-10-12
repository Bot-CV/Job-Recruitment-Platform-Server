package org.toanehihi.jobrecruitmentplatformserver.application.job.category.service;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobFamily;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;

import java.util.List;

public interface JobCategoryService {
    public PageResult<JobFamily> getJobFamily(int page, int size, String sortBy, String sortDir);
}
