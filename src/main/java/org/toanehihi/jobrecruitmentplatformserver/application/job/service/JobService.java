package org.toanehihi.jobrecruitmentplatformserver.application.job.service;

import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.*;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;

import java.util.List;

public interface JobService {
    JobDetailResponse getJobDetail(Long id);

    PageResult<JobResponse> getAllJobs(int page, int size, String sortBy, String sortDir);

//    PageResult<JobResponse> getPublishJobs(int page, int size, String sortBy, String sortDir);

    JobResponse createJob(Account account, CreateJobRequest createJobRequest);

    JobResponse updateJob(Account account, Long id, UpdateJobRequest request);

    JobResponse cancelJob(Account account, Long id);

    JobResponse moderateJobPosting(Account account, Long id, String action);

    PageResult<JobResponse> searchJobByTitle(JobSearchRequest request);

    List<JobResponse> recommendJobs(Account account);

    void deleteJob(Long id);

}
