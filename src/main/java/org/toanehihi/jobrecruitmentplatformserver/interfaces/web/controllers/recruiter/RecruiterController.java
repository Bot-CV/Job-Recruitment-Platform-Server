package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.recruiter;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service.RecruiterService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.CreateInterviewRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.InterviewResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.UpdateInterviewRequest;
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

    @PutMapping("/company")
    DataResponse<CompanyResponse> updateCompany(@RequestBody CompanyRequest request) {
        return DataResponse.<CompanyResponse>builder()
                .data(recruiterService.updateCompany(request))
                .build();
    }

    @PostMapping("/avatar")
    DataResponse<ResourceResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(recruiterService.updateAvatar(file))
                .build();
    }

    @GetMapping("/company/jobs")
    DataResponse<PageResult<JobResponse>> getCompanyJobs(
            @CurrentUser Account account,
            @RequestParam(value = "jobStatus") String jobStatus,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "datePosted") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "DESC") String sortDir) {
        return DataResponse.<PageResult<JobResponse>>builder()
                .data(recruiterService.getCompanyJobs(account, jobStatus, page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/company/{jobId}/applicants")
    DataResponse<PageResult<JobApplicantResponse>> getJobApplicants(
            @CurrentUser Account account,
            @PathVariable Long jobId,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "appliedAt") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "DESC") String sortDir) {
        return DataResponse.<PageResult<JobApplicantResponse>>builder()
                .data(recruiterService.getJobApplicants(account, jobId, page, size, sortBy, sortDir))
                .build();
    }

    @PostMapping("/company/applicants/{jobApplicationId}")
    DataResponse<JobApplicantResponse> processCandidate(
            @CurrentUser Account account,
            @PathVariable Long jobApplicationId,
            @RequestParam(value = "action") ApplicationStatus action
    ){
        return DataResponse.<JobApplicantResponse>builder()
                .data(recruiterService.processCandidate(account, jobApplicationId, action))
                .build();
    }

    @PostMapping("/company/applicants/interview")
    DataResponse<InterviewResponse> scheduleInterview(
            @CurrentUser Account account,
            @RequestBody CreateInterviewRequest request) {
        return DataResponse.<InterviewResponse>builder()
                .data(recruiterService.scheduleInterview(account, request))
                .build();
    }

    @PatchMapping("/company/applicants/interview")
    DataResponse<InterviewResponse> updateInterview(
            @CurrentUser Account account,
            @RequestBody UpdateInterviewRequest request) {
        return DataResponse.<InterviewResponse>builder()
                .data(recruiterService.updateInterview(account, request))
                .build();
    }

    @GetMapping("/company/applicants/interview")
    DataResponse<PageResult<InterviewResponse>> getAllInterviews(
            @CurrentUser Account account,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "scheduledAt") String sortBy,
            @RequestParam(value = "sortDir", required = false, defaultValue = "DESC") String sortDir) {
        return DataResponse.<PageResult<InterviewResponse>>builder()
                .data(recruiterService.getAllInterviews(account, page, size, sortBy, sortDir))
                .build();
    }
}
