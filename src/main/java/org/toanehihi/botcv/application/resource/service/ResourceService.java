package org.toanehihi.botcv.application.resource.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResumeAnalysisResponse;

public interface ResourceService {
    ResourceResponse updateUserAvatar(Account account, MultipartFile avatar);

    ResourceResponse updateCompanyLogo(Account account, MultipartFile logo);

    List<ResourceResponse> uploadAttestation(Account account, List<MultipartFile> files);

    ResourceResponse uploadResume(Account account, MultipartFile file);

    ResumeAnalysisResponse analyzeResume(Long resourceId);
}
