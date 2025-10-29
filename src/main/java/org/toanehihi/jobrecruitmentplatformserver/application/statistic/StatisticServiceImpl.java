package org.toanehihi.jobrecruitmentplatformserver.application.statistic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.application.job.service.JobService;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Job;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Recruiter;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.ApplicationStatus;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.JobStatus;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.statistic.StatisticResponse;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
    private final JobService jobService;
    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final JobApplicationRepository jobApplicationRepository;

    @Override
    public StatisticResponse getPlatformStatistics(Account account) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Long currentPublishJobCount = jobRepository.countByCompany_IdAndStatus(recruiter.getCompany().getId(), JobStatus.PUBLISHED);

        Long totalNewApplicationCount = jobApplicationRepository.countByJob_Company_IdAndStatus(recruiter.getCompany().getId(), ApplicationStatus.INTERVIEW);

        Long totalPendingApplicationCount = jobApplicationRepository.countByJob_Company_IdAndStatus(recruiter.getCompany().getId(), ApplicationStatus.SUBMITTED);


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
                    endOfWeek
            );
            weeklyApplicationCount.put(currentWeek - i, count);
        }

        return StatisticResponse.builder()
                .currentPublishJobCount(currentPublishJobCount)
                .totalNewApplicationCount(totalNewApplicationCount)
                .totalPendingApplicationCount(totalPendingApplicationCount)
                .weeklyApplicationCount(new TreeMap<>(weeklyApplicationCount))
                .build();
    }
}
