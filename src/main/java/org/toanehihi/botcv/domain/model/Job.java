package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.EmploymentType;
import org.toanehihi.botcv.domain.model.enums.JobStatus;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "jobs")
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id")
    private Recruiter recruiter;

    @Column(name = "title")
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private JobCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "seniority", columnDefinition = "seniority_level")
    private SeniorityLevel seniority;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", columnDefinition = "employment_type")
    private EmploymentType employmentType;

    @Column(name = "min_experience_years")
    private Integer minExperienceYears;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_mode", columnDefinition = "work_mode")
    private WorkMode workMode;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(name = "currency")
    private String currency;

    @Column(name = "max_candidates")
    private Integer maxCandidates;

    @OneToOne(mappedBy = "job")
    private JobDescription description;

    @Column(name = "date_posted")
    private OffsetDateTime datePosted;

    @Column(name = "date_expires")
    private OffsetDateTime dateExpires;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "job_status")
    private JobStatus status;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

    @ManyToMany
    @JoinTable(
        name = "job_skill_requirements",
        joinColumns = @JoinColumn(name = "job_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();
}
