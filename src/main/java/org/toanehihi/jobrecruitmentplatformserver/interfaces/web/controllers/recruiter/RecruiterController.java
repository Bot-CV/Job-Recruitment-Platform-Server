package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.recruiter;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service.RecruiterService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/recruiters")
@RequiredArgsConstructor
public class RecruiterController {

    private final RecruiterService recruiterService;

    @GetMapping("/profile")
    DataResponse<RecruiterResponse> getProfile() {
        return DataResponse.<RecruiterResponse>builder()
                .data(recruiterService.getProfile())
                .build();
    }

    @PutMapping("/profile")
    DataResponse<RecruiterResponse> updateRecruiterProfile(@RequestBody RecruiterRequest request) {
        return DataResponse.<RecruiterResponse>builder()
                .data(recruiterService.updateProfile(request))
                .build();
    }

    @PostMapping("/avatar")
    DataResponse<ResourceResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(recruiterService.updateAvatar(file))
                .build();
    }

    @GetMapping("/company/jobs")
    DataResponse<Page<JobResponse>> getCompanyJobs(
            @CurrentUser Account account,
            @RequestParam(value = "jobStatus") String jobStatus,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "datePosted") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "DESC") String sortDir) {
        return DataResponse.<Page<JobResponse>>builder()
                .data(recruiterService.getCompanyJobs(account, jobStatus, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/company/{jobId}/applicants")
    DataResponse<Page<JobApplicantResponse>> getJobApplicants(
            @CurrentUser Account account,
            @PathVariable Long jobId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "appliedAt") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "DESC") String sortDir) {
        return DataResponse.<Page<JobApplicantResponse>>builder()
                .data(recruiterService.getJobApplicants(account, jobId, page, size, sortBy, sortDir))
                .build();
    }

    @PatchMapping("/company/{jobId}/applicants/{jobApplicationId}")
    DataResponse<JobApplicantResponse> processCandidate(
            @CurrentUser Account account,
            @PathVariable Long jobApplicationId,
            @RequestParam(value = "action") String action
    ){
        return DataResponse.<JobApplicantResponse>builder()
                .data(recruiterService.processCandidate(account, jobApplicationId, action))
                .build();
    }
}
