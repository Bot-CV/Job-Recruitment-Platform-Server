package org.toanehihi.jobrecruitmentplatformserver.interfaces.web.controllers.resource;

import org.checkerframework.checker.units.qual.Current;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudStorageService;
import org.toanehihi.jobrecruitmentplatformserver.application.resource.ResourceService;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.annotation.CurrentUser;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.DataResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.FileData;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final CloudStorageService cloudStorageService;

    public ResourceController(ResourceService resourceService, CloudStorageService cloudStorageService) {
        this.resourceService = resourceService;
        this.cloudStorageService = cloudStorageService;
    }

    @PostMapping("/upload/avatar")
    public DataResponse<ResourceResponse> updateAvatar(@CurrentUser Account account, @RequestParam("file") MultipartFile file) {
        return DataResponse.<ResourceResponse>builder()
                .data(resourceService.updateUserAvatar(account, file))
                .build();
    }
    @PostMapping("/upload/company-logo")
    public DataResponse<ResourceResponse> updateCompanyLogo(@CurrentUser Account account, @RequestParam("file") MultipartFile file) {
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
}
