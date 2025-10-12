package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.job.category;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.toanehihi.jobrecruitmentplatformserver.application.job.category.service.JobCategoryService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobFamily;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;

import java.util.List;

@RestController
@RequestMapping("/api/v1/job/category")
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @GetMapping
    public DataResponse<PageResult<JobFamily>> getJobFamily(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return DataResponse.<PageResult<JobFamily>>builder()
                .data(jobCategoryService.getJobFamily(page, size, sortBy, sortDir))
                .build();
    }
}
