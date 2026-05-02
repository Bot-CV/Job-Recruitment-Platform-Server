package org.toanehihi.botcv.application.job.category.service;

import org.toanehihi.botcv.domain.model.JobCategory;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.job.category.CreateCategoryRequest;

import java.util.List;

public interface JobCategoryService {
    PageResult<JobCategory> getCategories(int page, int size, String sortBy, String sortDir);
    JobCategory createCategory(Long parentId, CreateCategoryRequest request);
    List<JobCategory> getRootCategories();
    List<JobCategory> getChildCategories(Long parentId);
}
