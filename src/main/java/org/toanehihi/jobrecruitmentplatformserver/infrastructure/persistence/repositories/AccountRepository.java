package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.toanehihi.jobrecruitmentplatformserver.domain.model.Account;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByEmail(String email);

	boolean existsByEmail(String email);

	@Query(value = """
			SELECT COUNT(a)
			FROM Account a
			WHERE a.dateCreated >= :startDate
			AND a.dateCreated < :endDate
			""")
	Long countAccountsCreatedBetween(@Param("startDate") OffsetDateTime startDate,
			@Param("endDate") OffsetDateTime endDate);
}
