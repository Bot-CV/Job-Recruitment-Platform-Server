package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Analytic;

public interface AnalyticRepository extends JpaRepository<Analytic, Long> {
}
