package org.toanehihi.botcv.infrastructure.persistence.mappers.job;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.SavedJob;
import org.toanehihi.botcv.interfaces.web.dtos.job.SavedJobResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SavedJobMapper {
    private final JobMapper jobMapper;

    public SavedJobResponse toResponse(SavedJob savedJob) {
        return SavedJobResponse.builder()
                .id(savedJob.getId())
                .job(jobMapper.toResponse(savedJob.getJob()))
                .savedAt(savedJob.getSavedAt())
                .build();
    }
}
