package org.toanehihi.jobrecruitmentplatformserver.application.resource;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResumeAnalysisResponse;

public interface ResourceService {
    ResourceResponse updateUserAvatar(Account account, MultipartFile avatar);

    ResourceResponse updateCompanyLogo(Account account, MultipartFile logo);

    List<ResourceResponse> uploadAttestation(Account account, List<MultipartFile> files);

    ResourceResponse uploadResume(Account account, MultipartFile file);

    ResumeAnalysisResponse analyzeResume(Long resourceId);
}
