package org.toanehihi.jobrecruitmentplatformserver.application.candidate.service;

import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.candidate.CandidateResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.SavedJobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicationResponse;

public interface CandidateService {

    CandidateResponse getProfile();

    CandidateResponse updateProfile(CandidateRequest request);

    SavedJobResponse saveJob(Long jobId);

    void removeSavedJob(Long jobId);

    JobApplicationResponse applyJob(Long jobId, MultipartFile cv);

    PageResult<JobApplicationResponse> getAllApplications(int page, int size, String sortBy, String sortDir);

    PageResult<SavedJobResponse> getAllSavedJobs(int page, int size, String sortBy, String sortDir);
}
