package org.toanehihi.botcv.interfaces.web.controllers.job.category;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.toanehihi.botcv.application.job.category.service.JobCategoryService;
import org.toanehihi.botcv.domain.model.JobCategory;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.job.category.CreateCategoryRequest;

import java.util.List;

@RestController
@RequestMapping("/api/jobs/categories")
@RequiredArgsConstructor
public class JobCategoryController {
    private final JobCategoryService jobCategoryService;

    @GetMapping("/public")
    public DataResponse<PageResult<JobCategory>> getCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return DataResponse.<PageResult<JobCategory>>builder()
                .data(jobCategoryService.getCategories(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/public/roots")
    public DataResponse<List<JobCategory>> getRootCategories() {
        return DataResponse.<List<JobCategory>>builder()
                .data(jobCategoryService.getRootCategories())
                .build();
    }

    @GetMapping("/public/{parentId}/children")
    public DataResponse<List<JobCategory>> getChildCategories(@PathVariable Long parentId) {
        return DataResponse.<List<JobCategory>>builder()
                .data(jobCategoryService.getChildCategories(parentId))
                .build();
    }

    @PostMapping
    public DataResponse<JobCategory> createCategory(
            @RequestParam(required = false) Long parentId,
            @RequestBody CreateCategoryRequest request) {
        return DataResponse.<JobCategory>builder()
                .data(jobCategoryService.createCategory(parentId, request))
                .build();
    }
}
