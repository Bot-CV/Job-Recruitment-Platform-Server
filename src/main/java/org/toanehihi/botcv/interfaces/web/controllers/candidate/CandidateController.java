package org.toanehihi.botcv.interfaces.web.controllers.candidate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.candidate.service.CandidateService;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.UserProfileBasedResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.botcv.interfaces.web.dtos.job.application.JobApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@RestController
@RequestMapping("api/candidates")
@RequiredArgsConstructor
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping("/profile")
    DataResponse<CandidateResponse> getProfile() {
        return DataResponse.<CandidateResponse>builder()
                .data(candidateService.getProfile())
                .build();
    }

    @PutMapping("/profile")
    DataResponse<CandidateResponse> updateCandidateProfile(@RequestBody CandidateRequest request) {
        return DataResponse.<CandidateResponse>builder()
                .data(candidateService.updateProfile(request))
                .build();
    }

    @PostMapping("/save/{jobId}")
    DataResponse<SavedJobResponse> saveJob(@PathVariable Long jobId) {
        return DataResponse.<SavedJobResponse>builder()
                .data(candidateService.saveJob(jobId))
                .build();
    }

    @DeleteMapping("/save/{jobId}")
    DataResponse<String> removeSavedJob(@PathVariable Long jobId) {
        candidateService.removeSavedJob(jobId);
        return DataResponse.<String>builder()
                .data("Remove saved job successfully")
                .build();
    }

    @PostMapping("/applications/{jobId}")
    DataResponse<JobApplicationResponse> applyJob(@PathVariable Long jobId, @RequestParam("file") MultipartFile file) {
        return DataResponse.<JobApplicationResponse>builder()
                .data(candidateService.applyJob(jobId, file))
                .build();
    }

    @GetMapping("/applications")
    DataResponse<PageResult<JobApplicationResponse>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return DataResponse.<PageResult<JobApplicationResponse>>builder()
                .data(candidateService.getAllApplications(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/saved-jobs")
    DataResponse<PageResult<SavedJobResponse>> getAllSavedJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "savedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return DataResponse.<PageResult<SavedJobResponse>>builder()
                .data(candidateService.getAllSavedJobs(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/resumes")
    DataResponse<PageResult<ResourceResponse>> getCandidateResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return DataResponse.<PageResult<ResourceResponse>>builder()
                .data(candidateService.getCandidateResumes(page, size, sortBy, sortDir))
                .build();
    }

    @GetMapping("/profile/{candidateId}")
    DataResponse<UserProfileBasedResponse> getUserProfileBasedData(@PathVariable Long candidateId) {
        return DataResponse.<UserProfileBasedResponse>builder()
                .data(candidateService.getUserProfileBasedData(candidateId))
                .build();
    }
}
