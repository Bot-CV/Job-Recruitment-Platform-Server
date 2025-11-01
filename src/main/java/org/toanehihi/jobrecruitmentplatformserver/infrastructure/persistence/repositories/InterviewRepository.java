package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Interview;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
}
