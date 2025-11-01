package org.toanehihi.jobrecruitmentplatformserver.application.recruiter.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.CreateInterviewRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.InterviewResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.UpdateInterviewRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.application.JobApplicantResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.recruiter.RecruiterResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.resource.ResourceResponse;

public interface RecruiterService {
    RecruiterResponse getProfile();

    RecruiterResponse updateProfile(RecruiterRequest request);

    CompanyResponse updateCompany(CompanyRequest request);

    ResourceResponse updateAvatar(MultipartFile file);

    Page<JobResponse> getCompanyJobs(Account account, String jobStatus, int page, int size, String sortBy,
            String sortDir);

    Page<JobApplicantResponse> getJobApplicants(Account account, Long jobId, int page, int size, String sortBy,
            String sortDir);

    JobApplicantResponse processCandidate(Account account, Long jobApplicationId, ApplicationStatus action);

    InterviewResponse scheduleInterview(Account account, CreateInterviewRequest request);

    InterviewResponse updateInterview(Account account, UpdateInterviewRequest request);

    PageResult<InterviewResponse> getAllInterviews(Account account, int page, int size, String sortBy, String sortDir);

}
