package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Interview;
import org.toanehihi.jobrecruitmentplatformserver.interfaces.web.dtos.interview.InterviewResponse;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    // Pagination company interviews
    Page<Interview> findByJobApplication_Job_Company_Id(Long companyId, Pageable pageable);
}
