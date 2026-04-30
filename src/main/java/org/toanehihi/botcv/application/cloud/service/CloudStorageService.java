package org.toanehihi.botcv.application.cloud.service;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.botcv.interfaces.web.dtos.resource.FileData;

public interface CloudStorageService {
    public CloudinaryFileInfo storeFile(MultipartFile file, String folderName);

    public FileData downloadFile(String resourceUrl);

    public void deleteFile(String imageUrl);
}
