package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.recruiter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service.RecruiterService;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
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

    @PutMapping("/avatar")
    DataResponse<ResourceResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(recruiterService.updateAvatar(file))
                .build();
    }
}
