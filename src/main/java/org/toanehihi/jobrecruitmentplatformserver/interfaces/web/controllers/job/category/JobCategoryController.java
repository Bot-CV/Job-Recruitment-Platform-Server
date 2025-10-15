package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.job.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.toanehihi.jobrecruitmentplatformserver.application.job.category.service.JobCategoryService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobFamily;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobRole;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.SubFamily;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.category.CreateCategoryRequest;

@RestController
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    @Autowired
    public JobCategoryController(JobCategoryService jobCategoryService) {
        this.jobCategoryService = jobCategoryService;
    }

    @GetMapping("/api/public/job/category")
    public DataResponse<PageResult<JobFamily>> getJobFamily(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return DataResponse.<PageResult<JobFamily>>builder()
                .data(jobCategoryService.getJobFamily(page, size, sortBy, sortDir))
                .build();
    }

    @PostMapping("/api/job/category")
    public DataResponse<JobFamily> createJobFamily(@RequestBody CreateCategoryRequest request) {
        return DataResponse.<JobFamily>builder()
                .data(jobCategoryService.createJobFamily(request))
                .build();
    }

    @PostMapping("/api/job/category/{jobFamilyId}")
    public DataResponse<SubFamily> getJobFamilyById(@PathVariable Long jobFamilyId, @RequestBody CreateCategoryRequest request) {
        return DataResponse.<SubFamily>builder()
                .data(jobCategoryService.createSubFamily(jobFamilyId, request))
                .build();
    }
    @PostMapping("/api/job/category/{jobFamilyId}/{subFamilyId}")
    public DataResponse<JobRole> createJobRole(@PathVariable Long subFamilyId, @RequestBody CreateCategoryRequest request) {
        return DataResponse.<JobRole>builder()
                .data(jobCategoryService.createJobRole(subFamilyId, request))
                .build();
    }
}
