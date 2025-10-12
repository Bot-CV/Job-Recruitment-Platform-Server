package org.toanehihi.jobrecruitmentplatformserver.application.cloud.service;

import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CloudStorageService {
    public Map<String, String> storeFile(MultipartFile file, String folderName);

    public void deleteFile(String imageUrl);
}
