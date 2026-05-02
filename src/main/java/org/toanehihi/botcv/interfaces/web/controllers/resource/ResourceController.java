package org.toanehihi.botcv.interfaces.web.controllers.resource;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.cloud.service.CloudStorageService;
import org.toanehihi.botcv.application.resource.service.ResourceService;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.interfaces.annotation.CurrentUser;
import org.toanehihi.botcv.interfaces.web.dtos.DataResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.FileData;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResumeAnalysisResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

	private final ResourceService resourceService;
	private final CloudStorageService cloudStorageService;

	@PostMapping("/upload/avatar")
	public DataResponse<ResourceResponse> updateAvatar(@CurrentUser Account account,
			@RequestParam("file") MultipartFile file) {
		return DataResponse.<ResourceResponse>builder()
				.data(resourceService.updateUserAvatar(account, file))
				.build();
	}

	@PostMapping("/upload/company-logo")
	public DataResponse<ResourceResponse> updateCompanyLogo(@CurrentUser Account account,
			@RequestParam("file") MultipartFile file) {
		return DataResponse.<ResourceResponse>builder()
				.data(resourceService.updateCompanyLogo(account, file))
				.build();
	}

	@GetMapping("/download")
	public DataResponse<FileData> downloadResource(@RequestParam(value = "url") String resourceUrl) {
		return DataResponse.<FileData>builder()
				.data(cloudStorageService.downloadFile(resourceUrl))
				.build();
	}

	@GetMapping("/resumes/analyze")
	DataResponse<ResumeAnalysisResponse> analyzeResume(@RequestParam("resourceId") Long resourceId) {
		return DataResponse.<ResumeAnalysisResponse>builder()
				.data(resourceService.analyzeResume(resourceId))
				.build();
	}

    @PostMapping("/upload/resume")
    DataResponse<ResourceResponse> uploadResume(@CurrentUser Account account,
            @RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(resourceService.uploadResume(account, file))
                .build();
    }
}
