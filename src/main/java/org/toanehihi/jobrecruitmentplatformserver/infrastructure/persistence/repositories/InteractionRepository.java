    package org.toanehihi.jobrecruitmentplatformserver.infrastructure.persistence.repositories;

    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;
    import org.toanehihi.jobrecruitmentplatformserver.domain.model.UserInteraction;

    @Repository
    public interface InteractionRepository extends JpaRepository<UserInteraction, Long> {
    }
