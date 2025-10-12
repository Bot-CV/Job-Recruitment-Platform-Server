package org.toanehihi.jobrecruitmentplatformserver.application.cloud.service;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;

public interface CloudStorageService {
    public CloudinaryFileInfo storeFile(MultipartFile file, String folderName);

    public void deleteFile(String imageUrl);
}
