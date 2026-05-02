package org.toanehihi.botcv.application.job.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.domain.model.JobCategory;
import org.toanehihi.botcv.infrastructure.persistence.repositories.JobCategoryRepository;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.job.category.CreateCategoryRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobCategoryServiceImpl implements JobCategoryService {
    private final JobCategoryRepository jobCategoryRepository;

    @Override
    public PageResult<JobCategory> getCategories(int page, int size, String sortBy, String sortDir) {
        Sort.Direction direction = sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        return PageResult.from(jobCategoryRepository.findAll(pageable));
    }

    @Override
    public JobCategory createCategory(Long parentId, CreateCategoryRequest request) {
        JobCategory.JobCategoryBuilder builder = JobCategory.builder()
                .name(request.getName())
                .slug(request.getName().toLowerCase().replace(" ", "-"));

        if (parentId != null) {
            JobCategory parent = jobCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
            builder.parent(parent);
        }

        return jobCategoryRepository.save(builder.build());
    }

    @Override
    public List<JobCategory> getRootCategories() {
        return jobCategoryRepository.findByParentIsNull();
    }

    @Override
    public List<JobCategory> getChildCategories(Long parentId) {
        return jobCategoryRepository.findByParentId(parentId);
    }
}
