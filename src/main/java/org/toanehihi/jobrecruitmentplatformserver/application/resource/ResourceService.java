package org.toanehihi.jobrecruitmentplatformserver.application.resource;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

public interface ResourceService {
    ResourceResponse updateUserAvatar(Account account, MultipartFile avatar);

    ResourceResponse updateCompanyLogo(Account account, MultipartFile logo);

    ResourceResponse uploadJobAttachment(MultipartFile file); // For job image and attachment

    List<ResourceResponse> uploadAttestation(Account account, List<MultipartFile> files);

    ResourceResponse uploadResume(Account account, MultipartFile file);

    Map<String, Object> analyzeResume(Long resourceId);
}
