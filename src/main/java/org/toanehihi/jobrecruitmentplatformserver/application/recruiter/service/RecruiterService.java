package org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

public interface RecruiterService {
    RecruiterResponse getProfile();

    RecruiterResponse updateProfile(RecruiterRequest request);

    CompanyResponse updateCompany(CompanyRequest request);

    ResourceResponse updateAvatar(MultipartFile file);
}
