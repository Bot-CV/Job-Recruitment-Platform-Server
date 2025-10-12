package org.toanehihi.jobrecruitmentplatformserver.application.job.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.JobFamily;
import org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories.JobFamilyRepository;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.PageResult;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {
    private final JobFamilyRepository jobFamilyRepository;

    @Override
    public PageResult<JobFamily> getJobFamily(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page jobFamily = jobFamilyRepository.findAll(pageable);

        return PageResult.from(jobFamily);
    }
}
