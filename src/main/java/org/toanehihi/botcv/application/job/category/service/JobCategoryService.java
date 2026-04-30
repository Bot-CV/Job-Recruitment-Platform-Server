package org.toanehihi.botcv.application.job.category.service;

import org.toanehihi.botcv.domain.model.JobFamily;
import org.toanehihi.botcv.domain.model.JobRole;
import org.toanehihi.botcv.domain.model.SubFamily;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.job.category.CreateCategoryRequest;

public interface JobCategoryService {
    public PageResult<JobFamily> getJobFamily(int page, int size, String sortBy, String sortDir);
    public JobFamily createJobFamily(CreateCategoryRequest request);
    public SubFamily createSubFamily(Long jobFamilyId,CreateCategoryRequest request);
    public JobRole createJobRole(Long subFamilyId,CreateCategoryRequest request);
}
