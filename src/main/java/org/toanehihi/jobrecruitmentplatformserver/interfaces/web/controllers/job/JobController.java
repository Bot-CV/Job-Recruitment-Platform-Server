package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.job;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.toanehihi.jobrecruitmentplatformserver.application.job.service.JobService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.CreateJobRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobDetailResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.UpdateJobRequest;

@RestController
@RequestMapping("/api/job")
@AllArgsConstructor
public class JobController {
    private final JobService jobService;

    @GetMapping
    public DataResponse<PageResult<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return DataResponse.<PageResult<JobResponse>>builder()
                .data(jobService.getAllJobs(page, size, sortBy, sortDir))
                .build();
    }

    @PostMapping
    public DataResponse<JobResponse> createJob(@CurrentUser Account account, @RequestBody CreateJobRequest request) {
        return DataResponse.<JobResponse>builder()
                .data(jobService.createJob(account, request))
                .build();
    }

    @PutMapping("/{jobId}")
    public DataResponse<JobResponse> updateJob(@CurrentUser Account account, @PathVariable Long jobId, @RequestBody UpdateJobRequest request){
        return DataResponse.<JobResponse>builder()
                .data(jobService.updateJob(account, jobId, request))
                .build();
    }

    @PatchMapping("/cancel/{jobId}")
    public DataResponse<JobResponse> cancelJob(@CurrentUser Account account, @PathVariable Long jobId){
        return DataResponse.<JobResponse>builder()
                .data(jobService.cancelJob(account, jobId))
                .build();
    }

    @PatchMapping("/{jobId}/moderate")
    public DataResponse<JobResponse> moderateJobPosting(@CurrentUser Account account, @PathVariable Long jobId, @RequestParam String action){
        return DataResponse.<JobResponse>builder()
                .data(jobService.moderateJobPosting(account, jobId, action))
                .build();
    }

    @GetMapping("/public/detail/{jobId}")
    public DataResponse<JobDetailResponse> getJobDetail(@PathVariable Long jobId){
        return DataResponse.<JobDetailResponse>builder()
                .data(jobService.getJobDetail(jobId))
                .build();
    }
}
