package org.toanehihi.botcv.application.job.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.model.JobFamily;
import org.toanehihi.botcv.domain.model.JobRole;
import org.toanehihi.botcv.domain.model.SubFamily;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobFamilyRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobRoleRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.SubFamilyRepository;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.job.category.CreateCategoryRequest;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {
    private final JobFamilyRepository jobFamilyRepository;
    private final SubFamilyRepository subFamilyRepository;
    private final JobRoleRepository jobRoleRepository;

    @Override
    public PageResult<JobFamily> getJobFamily(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return PageResult.from(jobFamilyRepository.findAll(pageable));
    }

    @Override
    public JobFamily createJobFamily(CreateCategoryRequest request) {
        JobFamily jobFamily = JobFamily.builder()
                .name(request.getName())
                .slug(request.getName().toLowerCase().replace(" ", "-"))
                .build();
        return jobFamilyRepository.save(jobFamily);
    }

    @Override
    public SubFamily createSubFamily(Long jobFamilyId, CreateCategoryRequest request) {
        SubFamily subFamily = SubFamily.builder()
                .name(request.getName())
                .jobFamily(JobFamily.builder().id(jobFamilyId).build())
                .build();
        return subFamilyRepository.save(subFamily);
    }

    @Override
    public JobRole createJobRole(Long subFamilyId, CreateCategoryRequest request) {
        JobRole jobRole = JobRole.builder()
                .name(request.getName())
                .subFamily(SubFamily.builder().id(subFamilyId).build())
                .build();
        return jobRoleRepository.save(jobRole);
    }


}
