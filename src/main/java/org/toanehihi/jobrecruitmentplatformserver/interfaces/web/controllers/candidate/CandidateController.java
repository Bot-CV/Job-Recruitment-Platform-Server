package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.candidate;

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
import org.toanehihi.jobrecruitmentplatformserver.application.candidate.service.CandidateService;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

import lombok.RequiredArgsConstructor;

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
        return DataResponse.<String>builder()
                .data("Remove saved job successfully")
                .build();
    }

    @PostMapping("/avatar")
    DataResponse<ResourceResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(candidateService.updateAvatar(file))
                .build();
    }
}
