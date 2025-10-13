package org.toanehihi.jobrecruitmentplatformserver.application.candidate.service;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

public interface CandidateService {

    CandidateResponse getProfile();

    CandidateResponse updateProfile(CandidateRequest request);

    SavedJobResponse saveJob(Long jobId);

    void removeSavedJob(Long jobId);

    ResourceResponse updateAvatar(MultipartFile file);

    
}
