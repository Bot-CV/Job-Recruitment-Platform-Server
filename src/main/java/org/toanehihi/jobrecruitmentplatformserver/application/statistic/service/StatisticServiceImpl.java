package org.toanehihi.jobrecruitmentplatformserver.application.statistic.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.RoleName;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.job.JobMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.AccountRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.job.JobResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.AdminStatisticResponse;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.NewestJobApplication;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.StatisticResponse;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final AccountRepository accountRepository;
    private final CandidateRepository candidateRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final JobMapper jobMapper;

    @Override
    public StatisticResponse getPlatformStatistics(Account account) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Long currentPublishJobCount = jobRepository.countByCompany_IdAndStatus(recruiter.getCompany().getId(),
                JobStatus.PUBLISHED);

        Long totalNewApplicationCount = jobApplicationRepository.countByJob_Company_IdAndStatus(
                recruiter.getCompany().getId(), ApplicationStatus.INTERVIEW);

        Long totalPendingApplicationCount = jobApplicationRepository.countByJob_Company_IdAndStatus(
                recruiter.getCompany().getId(), ApplicationStatus.SUBMITTED);

        ZonedDateTime now = ZonedDateTime.now();

        Map<Integer, Long> weeklyApplicationCount = new HashMap<>();

        for (int i = 0; i <= 7; i++) {
            int currentWeek = now.get(WeekFields.ISO.weekOfYear());

            OffsetDateTime startOfWeek = now.with(WeekFields.ISO.weekOfYear(), currentWeek - i)
                    .with(WeekFields.ISO.dayOfWeek(), 1)
                    .toOffsetDateTime()
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
            OffsetDateTime endOfWeek = now.with(WeekFields.ISO.weekOfYear(), currentWeek - i)
                    .with(WeekFields.ISO.dayOfWeek(), 7)
                    .toOffsetDateTime()
                    .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            Long count = jobApplicationRepository.countByJob_Company_IdAndAppliedAtBetween(
                    recruiter.getCompany().getId(),
                    startOfWeek,
                    endOfWeek);
            weeklyApplicationCount.put(currentWeek - i, count);
        }

        List<JobResponse> newestJobsList = jobRepository.findNewestJob(recruiter.getCompany().getId()).stream()
                .map(jobMapper::toResponse)
                .toList();

        List<NewestJobApplication> newestJobApplications = jobApplicationRepository
                .findTop3ByJobCompanyIdOrderByAppliedAtDesc(recruiter.getCompany().getId())
                .orElseThrow(() -> new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND))
                .stream()
                .map(jobApplication -> {
                    return NewestJobApplication.builder()
                            .candidateName(jobApplication.getCandidate().getFullName())
                            .jobTitle(jobApplication.getJob().getTitle())
                            .appliedAt(jobApplication.getAppliedAt())
                            .build();
                }).toList();

        return StatisticResponse.builder()
                .currentPublishJobCount(currentPublishJobCount != 0 ? currentPublishJobCount : 0)
                .totalNewApplicationCount(totalNewApplicationCount != 0 ? totalNewApplicationCount : 0)
                .totalPendingApplicationCount(
                        totalPendingApplicationCount != 0 ? totalPendingApplicationCount : 0)
                .weeklyApplicationCount(new TreeMap<>(weeklyApplicationCount))
                .newestJobApplications(newestJobApplications)
                .newestJobs(newestJobsList)
                .build();
    }

    @Override
    public AdminStatisticResponse getAdminStatistics(Account account) {
        if (!account.getRole().getName().equals(RoleName.ADMIN.name())) {
            throw new AppException(ErrorCode.ACCESS_FORBIDDEN);
        }
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startWeek = now.with(DayOfWeek.MONDAY).truncatedTo(ChronoUnit.DAYS);
        OffsetDateTime endWeek = startWeek.plusWeeks(1);

        return AdminStatisticResponse.builder()
                .totalAccount(accountRepository.count())
                .totalCandidate(candidateRepository.count())
                .totalRecruiter(recruiterRepository.count())
                .totalCompany(companyRepository.count())
                .totalJob(jobRepository.count())
                .pendingCompanyVerification(companyRepository.countUnverifiedCompanies())
                .pendingJobApproval(jobRepository.countByStatus(JobStatus.PENDING))
                .weeklyNewAccount(accountRepository.countAccountsCreatedBetween(startWeek, endWeek))
                .weeklyNewJob(jobRepository.countJobCreatedBetweenWithStatus(startWeek, endWeek, JobStatus.PUBLISHED))
                .build();
    }
}
