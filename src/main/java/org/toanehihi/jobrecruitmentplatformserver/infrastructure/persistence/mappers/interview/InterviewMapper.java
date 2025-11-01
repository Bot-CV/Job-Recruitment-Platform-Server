package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.interview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.AppException;
import org.toanehihi.jobrecruitmentplatformserver.domain.exception.ErrorCode;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Interview;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.enums.InterviewStatus;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobApplicationRepository;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.LocationRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.CreateInterviewRequest;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.InterviewResponse;

@Component
@RequiredArgsConstructor
public class InterviewMapper {
    private final LocationMapper locationMapper;
    private final JobApplicationRepository jobApplicationRepository;
    private final LocationRepository locationRepository;

    public InterviewResponse toResponse(Interview interview) {
        return InterviewResponse.builder()
                .id(interview.getId())
                .applicationId(interview.getJobApplication().getId())
                .scheduledAt(interview.getScheduledAt())
                .jobTitle(interview.getJobApplication().getJob().getTitle())
                .location(locationMapper.toResponse(interview.getLocation()))
                .candidateName(interview.getJobApplication().getCandidate().getFullName())
                .notes(interview.getNotes())
                .dateCreated(interview.getDateCreated())
                .dateUpdated(interview.getDateUpdated())
                .status(interview.getStatus())
                .build();
    }

    public Interview toEntity(CreateInterviewRequest request) {
        return Interview.builder()
                .scheduledAt(request.getScheduledAt())
                .jobApplication(jobApplicationRepository.findById(request.getApplicationId()).orElseThrow(() -> new AppException(ErrorCode.JOB_APPLICATION_NOT_FOUND)))
                .location(locationRepository.findById(request.getLocationId()).orElseThrow(() -> new AppException(ErrorCode.LOCATION_NOT_FOUND)))
                .status(InterviewStatus.SCHEDULED)
                .notes("")
                .build();
    }
}
