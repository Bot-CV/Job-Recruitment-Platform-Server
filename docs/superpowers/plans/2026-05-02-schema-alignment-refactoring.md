# Schema Alignment Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Align all JPA domain entities with the new V1__init.sql database schema — fix mismatches, create 11 new entities, remove 4 obsolete entities, rewire all affected services/mappers/DTOs/controllers.

**Architecture:** Bottom-up entity-first approach in 4 phases (A→D). Each phase must compile independently before proceeding. No tests required.

**Tech Stack:** Java 21, Spring Boot 3.5.6, JPA/Hibernate, Lombok, PostgreSQL, Flyway

**Base package:** `org.toanehihi.botcv`

---

## File Structure

### New files to create:
- `domain/model/enums/CompanySize.java`
- `domain/model/enums/ExperienceYears.java`
- `domain/model/enums/VerificationStatus.java`
- `domain/model/Permission.java`
- `domain/model/Moderator.java`
- `domain/model/CompanyVerification.java`
- `domain/model/CompanyVerificationAttachment.java`
- `domain/model/ids/CompanyVerificationAttachmentId.java`
- `domain/model/CandidateWorkExperience.java`
- `domain/model/School.java`
- `domain/model/CandidateEducation.java`
- `domain/model/CandidateResume.java`
- `domain/model/JobCategory.java`
- `domain/model/CandidatePosition.java`
- `domain/model/ids/CandidatePositionId.java`
- `infrastructure/persistence/repositories/PermissionRepository.java`
- `infrastructure/persistence/repositories/ModeratorRepository.java`
- `infrastructure/persistence/repositories/CompanyVerificationRepository.java`
- `infrastructure/persistence/repositories/CompanyVerificationAttachmentRepository.java`
- `infrastructure/persistence/repositories/CandidateWorkExperienceRepository.java`
- `infrastructure/persistence/repositories/SchoolRepository.java`
- `infrastructure/persistence/repositories/CandidateEducationRepository.java`
- `infrastructure/persistence/repositories/CandidateResumeRepository.java`
- `infrastructure/persistence/repositories/JobCategoryRepository.java`
- `infrastructure/persistence/repositories/CandidatePositionRepository.java`

### Files to delete:
- `domain/model/JobFamily.java`
- `domain/model/SubFamily.java`
- `domain/model/JobRole.java`
- `domain/model/AttestationResource.java`
- `domain/model/ids/AttestationResourceId.java`
- `infrastructure/persistence/repositories/AttestationResourceRepository.java`
- `infrastructure/persistence/repositories/JobFamilyRepository.java`
- `infrastructure/persistence/repositories/SubFamilyRepository.java`
- `infrastructure/persistence/repositories/JobRoleRepository.java`

### Files to modify:
- `domain/model/enums/ResourceType.java`
- `domain/model/enums/EmploymentType.java`
- `domain/model/Resource.java`
- `domain/model/Candidate.java`
- `domain/model/CandidateSkill.java`
- `domain/model/Company.java`
- `domain/model/Recruiter.java`
- `domain/model/Job.java`
- `domain/model/JobDescription.java`
- `domain/model/OutboxEvent.java`
- `domain/model/Interview.java`
- `domain/model/Role.java`
- `domain/model/JobApplication.java`
- `domain/model/CompanyLocation.java`
- `application/resource/service/ResourceServiceImpl.java`
- `application/candidate/service/CandidateServiceImpl.java`
- `application/company/service/CompanyServiceImpl.java`
- `application/job/service/JobServiceImpl.java`
- `application/outbox/service/OutboxEventService.java`
- `application/outbox/service/OutboxEventServiceImpl.java`
- `application/outbox/service/OutboxReconciliationService.java`
- `application/cloud/service/CloudStorageService.java`
- `application/cloud/service/CloudinaryStorageImpl.java`
- `infrastructure/persistence/mappers/resource/ResourceMapper.java`
- `infrastructure/persistence/mappers/candidate/CandidateMapper.java`
- `infrastructure/persistence/mappers/company/CompanyMapper.java`
- `infrastructure/persistence/mappers/job/JobMapper.java`
- `infrastructure/persistence/mappers/job/JobApplicationMapper.java`
- `infrastructure/persistence/mappers/recruiter/RecruiterMapper.java`
- `infrastructure/persistence/mappers/skill/CandidateSkillMapper.java`
- `infrastructure/persistence/repositories/CompanyRepository.java`
- `infrastructure/persistence/repositories/ResourceRepository.java`
- `infrastructure/persistence/repositories/OutboxEventRepository.java`
- `infrastructure/persistence/repositories/JobRepository.java`
- `interfaces/web/dtos/resource/ResourceResponse.java`
- `interfaces/web/dtos/candidate/CandidateRequest.java`
- `interfaces/web/dtos/candidate/CandidateResponse.java`
- `interfaces/web/dtos/company/CompanyResponse.java`
- `interfaces/web/dtos/job/CreateJobRequest.java`
- `interfaces/web/dtos/job/UpdateJobRequest.java`
- `interfaces/web/dtos/job/JobResponse.java`
- `interfaces/web/dtos/job/JobDetailResponse.java`
- `interfaces/web/dtos/job/JobEventPayload.java`
- `interfaces/web/dtos/skill/CandidateSkillRequest.java`
- `interfaces/web/dtos/skill/CandidateSkillResponse.java`

---

## Phase A: Entity Alignment + New Enums

### Task 1: Create new enums and fix existing enums

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/enums/CompanySize.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/enums/ExperienceYears.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/enums/VerificationStatus.java`
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/enums/ResourceType.java`
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/enums/EmploymentType.java`

- [ ] **Step 1: Create CompanySize enum**

```java
package org.toanehihi.botcv.domain.model.enums;

public enum CompanySize {
    MICRO, SMALL, MEDIUM, LARGE, ENTERPRISE;
}
```

- [ ] **Step 2: Create ExperienceYears enum**

```java
package org.toanehihi.botcv.domain.model.enums;

public enum ExperienceYears {
    NO_EXPERIENCE, UNDER_1_YEAR, ONE_YEAR, TWO_YEARS,
    THREE_YEARS, FOUR_YEARS, FIVE_YEARS, OVER_5_YEARS;
}
```

- [ ] **Step 3: Create VerificationStatus enum**

```java
package org.toanehihi.botcv.domain.model.enums;

public enum VerificationStatus {
    SUBMITTED, APPROVED, REJECTED;
}
```

- [ ] **Step 4: Replace ResourceType enum values**

Change from `AVATAR, CV, COMPANY_LOGO, JOB_ATTACHMENT, ATTESTATION` to:

```java
package org.toanehihi.botcv.domain.model.enums;

public enum ResourceType {
    IMAGE, DOCUMENT, VIDEO;
}
```

- [ ] **Step 5: Fix EmploymentType enum — remove FREELANCE, align with SQL**

SQL defines: `FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, VOLUNTEER, TEMPORARY`. Current Java has `FREELANCE` (not in SQL) and is missing nothing else. Replace:

```java
package org.toanehihi.botcv.domain.model.enums;

public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERNSHIP,
    VOLUNTEER,
    TEMPORARY;
}
```

- [ ] **Step 6: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/enums/CompanySize.java \
       src/main/java/org/toanehihi/botcv/domain/model/enums/ExperienceYears.java \
       src/main/java/org/toanehihi/botcv/domain/model/enums/VerificationStatus.java \
       src/main/java/org/toanehihi/botcv/domain/model/enums/ResourceType.java \
       src/main/java/org/toanehihi/botcv/domain/model/enums/EmploymentType.java
git commit -m "feat: add CompanySize, ExperienceYears, VerificationStatus enums; update ResourceType and EmploymentType"
```

---

### Task 2: Fix Resource entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Resource.java`

- [ ] **Step 1: Rewrite Resource.java to match SQL schema**

Remove fields: `ownerId`, `mimeType`, `url`. Add field: `size`. Fix `contentType` annotation.

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.toanehihi.botcv.domain.model.enums.ResourceType;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "resources")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, columnDefinition = "resource_type")
    private ResourceType resourceType;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "name", nullable = false)
    private String name;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private OffsetDateTime uploadedAt;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Resource.java
git commit -m "fix: align Resource entity with SQL schema — remove url/mimeType/ownerId, add size"
```

---

### Task 3: Fix Candidate entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Candidate.java`

- [ ] **Step 1: Rewrite Candidate.java**

Remove: `seniority` (SeniorityLevel), `salaryExpectMin`, `salaryExpectMax`. Add: `experienceYears` (ExperienceYears enum), `salaryExpect` (single Integer).

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.ExperienceYears;

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
@Table(name = "candidates")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_years", columnDefinition = "experience_years")
    private ExperienceYears experienceYears;

    @Column(name = "salary_expect")
    private Integer salaryExpect;

    @Column(name = "currency")
    private String currency;

    @Column(name = "remote_pref")
    private Boolean remotePref;

    @Column(name = "relocation_pref")
    private Boolean relocationPref;

    @Column(name = "avatar_resource_id")
    private Long avatarResourceId;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

    @OneToMany(mappedBy = "candidate", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CandidateSkill> skills = new HashSet<>();
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Candidate.java
git commit -m "fix: align Candidate entity — replace seniority/salaryExpectMin/Max with experienceYears/salaryExpect"
```

---

### Task 4: Fix CandidateSkill, CompanyLocation, and JobApplication entities

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/CandidateSkill.java`
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/CompanyLocation.java`
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/JobApplication.java`

- [ ] **Step 1: Remove `level` field from CandidateSkill**

The `candidate_skills` table is a pure join table with only `(candidate_id, skill_id)`. Remove the `level` field:

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CandidateSkillId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"candidate", "skill"})
@Entity
@Table(name = "candidate_skills")
@IdClass(CandidateSkillId.class)
public class CandidateSkill {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id")
    private Skill skill;
}
```

- [ ] **Step 2: Fix CompanyLocation table name**

Change `@Table(name = "company_location")` to `@Table(name = "company_locations")`.

- [ ] **Step 3: Fix JobApplication — add columnDefinition to status**

Add `columnDefinition = "application_status"` to the `@Column` on `status`:

```java
@Enumerated(EnumType.STRING)
@Column(name = "status", columnDefinition = "application_status")
@Builder.Default
private ApplicationStatus status = ApplicationStatus.SUBMITTED;
```

- [ ] **Step 4: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/CandidateSkill.java \
       src/main/java/org/toanehihi/botcv/domain/model/CompanyLocation.java \
       src/main/java/org/toanehihi/botcv/domain/model/JobApplication.java
git commit -m "fix: align CandidateSkill, CompanyLocation, JobApplication with SQL schema"
```

---

### Task 5: Fix Company entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Company.java`

- [ ] **Step 1: Rewrite Company.java**

Changes:
- `verified` column name: `"verified"` → `"is_verified"`
- `size` type: `String` → `CompanySize` enum with `@Enumerated` and `columnDefinition`
- Remove `Set<AttestationResource> attestations` collection
- Change `@OneToOne(mappedBy = "company") Recruiter recruiter` → `@OneToMany(mappedBy = "company") Set<Recruiter> recruiters`

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.CompanySize;

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
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "website")
    private String website;

    private String description;

    private String phone;

    private String email;

    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(name = "size", columnDefinition = "company_size")
    private CompanySize size;

    @Column(name = "logo_resource_id")
    private Long logoResourceId;

    @Column(name = "is_verified")
    @Builder.Default
    private boolean verified = false;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;

    @OneToMany(mappedBy = "company")
    @Builder.Default
    private Set<Recruiter> recruiters = new HashSet<>();

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CompanyLocation> companyLocations = new HashSet<>();
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Company.java
git commit -m "fix: align Company entity — CompanySize enum, is_verified column, OneToMany recruiters"
```

---

### Task 6: Fix Recruiter entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Recruiter.java`

- [ ] **Step 1: Fix Recruiter.java**

Changes:
- `@OneToOne(fetch = FetchType.LAZY, optional = false) Company company` → `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "company_id", nullable = false)`
- `avatarResourceId`: remove `nullable = false` (SQL allows null)

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "recruiters")
public class Recruiter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @Column(name = "full_name", length = 150)
    private String fullName;

    private String phone;

    @Column(name = "avatar_resource_id")
    private Long avatarResourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated", nullable = false)
    private OffsetDateTime dateUpdated;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Recruiter.java
git commit -m "fix: align Recruiter — ManyToOne company, nullable avatarResourceId"
```

---

### Task 7: Fix Job entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Job.java`

- [ ] **Step 1: Rewrite Job.java**

Changes:
- Remove `@ManyToOne JobRole jobRole`
- Add `@ManyToOne JobCategory category` (will reference entity created in Phase B — use forward reference)
- Add `@ManyToOne Recruiter recruiter`
- Add `@CreationTimestamp dateCreated`, `@UpdateTimestamp dateUpdated`
- Add `columnDefinition` annotations for enum columns

```java
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
```

Note: `JobCategory` will be created in Phase B Task 14. The `Job` entity references it here as a forward declaration — this will compile as long as the `JobCategory` class exists. We will create a minimal stub in Phase B if needed.

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Job.java
git commit -m "fix: align Job entity — replace jobRole with category, add recruiter/timestamps"
```

---

### Task 8: Fix JobDescription entity (structural PK fix)

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/JobDescription.java`

- [ ] **Step 1: Rewrite JobDescription.java**

SQL uses `job_id` as PRIMARY KEY (no separate `id`). Use `@MapsId` pattern. Fix table name to `job_descriptions`.

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "job_id")
    private Job job;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;

    @Column(name = "responsibilities", columnDefinition = "text")
    private String responsibilities;

    @Column(name = "requirements", columnDefinition = "text")
    private String requirements;

    @Column(name = "nice_to_have", columnDefinition = "text")
    private String niceToHave;

    @Column(name = "benefits", columnDefinition = "text")
    private String benefits;

    @Column(name = "hiring_process", columnDefinition = "text")
    private String hiringProcess;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/JobDescription.java
git commit -m "fix: JobDescription PK — use @MapsId with job_id as PK, fix table name"
```

---

### Task 9: Fix OutboxEvent entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/OutboxEvent.java`

- [ ] **Step 1: Rewrite OutboxEvent.java**

Changes: add `@Column(name = ...)` for `aggregateType`, `aggregateId`, `eventType`; change `aggregateId` from `Long` to `String`; fix `eventType` length to 50; add `processedAt` field.

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.enums.OutboxStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "outbox_events")
@Builder
@NoArgsConstructor
public class OutboxEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 64)
    private String aggregateId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private OffsetDateTime occurredAt = OffsetDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "outbox_status")
    @Builder.Default
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private int attempts = 0;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @Column(name = "trace_id")
    private UUID traceId;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/OutboxEvent.java
git commit -m "fix: align OutboxEvent — column annotations, aggregateId String, add processedAt"
```

---

### Task 10: Fix Interview and Role entities

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Interview.java`
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Role.java`

- [ ] **Step 1: Fix Interview.java**

Add `meetingUrl` field. Add `@Column` annotations for `scheduledAt`, `status`, `notes`.

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.toanehihi.botcv.domain.model.enums.InterviewStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "interviews")
public class Interview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id")
    private JobApplication jobApplication;

    @Column(name = "scheduled_at", nullable = false)
    private OffsetDateTime scheduledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @Column(name = "meeting_url", length = 500)
    private String meetingUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "interview_status")
    private InterviewStatus status;

    @Column(name = "notes")
    private String notes;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;
}
```

- [ ] **Step 2: Fix Role.java — add dateCreated**

```java
package org.toanehihi.botcv.domain.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "roles")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;
}
```

Note: The `permissions` ManyToMany will be added in Phase B Task 16 after the Permission entity is created.

- [ ] **Step 3: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Interview.java \
       src/main/java/org/toanehihi/botcv/domain/model/Role.java
git commit -m "fix: align Interview (meetingUrl, column annotations) and Role (dateCreated)"
```

---

## Phase B: New Entities + Repositories

### Task 11: Create Permission entity

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/Permission.java`

- [ ] **Step 1: Create Permission.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Permission.java
git commit -m "feat: create Permission entity"
```

---

### Task 12: Create Moderator entity

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/Moderator.java`

- [ ] **Step 1: Create Moderator.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "moderators")
public class Moderator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private OffsetDateTime dateUpdated;
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Moderator.java
git commit -m "feat: create Moderator entity"
```

---

### Task 13: Create CompanyVerification and CompanyVerificationAttachment entities

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CompanyVerification.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CompanyVerificationAttachment.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/ids/CompanyVerificationAttachmentId.java`

- [ ] **Step 1: Create CompanyVerification.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.enums.VerificationStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "company_verifications")
public class CompanyVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "verification_status")
    @Builder.Default
    private VerificationStatus status = VerificationStatus.SUBMITTED;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private Recruiter submittedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private Moderator reviewedBy;

    @Column(name = "submitted_at", nullable = false)
    @Builder.Default
    private OffsetDateTime submittedAt = OffsetDateTime.now();

    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;
}
```

- [ ] **Step 2: Create CompanyVerificationAttachmentId.java**

```java
package org.toanehihi.botcv.domain.model.ids;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CompanyVerificationAttachmentId implements Serializable {
    private Long verification;
    private Long resource;
}
```

- [ ] **Step 3: Create CompanyVerificationAttachment.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CompanyVerificationAttachmentId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"verification", "resource"})
@Entity
@Table(name = "company_verification_attachments")
@IdClass(CompanyVerificationAttachmentId.class)
public class CompanyVerificationAttachment {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id")
    private CompanyVerification verification;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;
}
```

- [ ] **Step 4: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/CompanyVerification.java \
       src/main/java/org/toanehihi/botcv/domain/model/CompanyVerificationAttachment.java \
       src/main/java/org/toanehihi/botcv/domain/model/ids/CompanyVerificationAttachmentId.java
git commit -m "feat: create CompanyVerification and CompanyVerificationAttachment entities"
```

---

### Task 14: Create JobCategory entity

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/JobCategory.java`

- [ ] **Step 1: Create JobCategory.java**

The `path` column uses PostgreSQL LTREE type, mapped as a String. Self-referencing parent/children relationship.

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "job_categories")
public class JobCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private JobCategory parent;

    @Column(name = "path", columnDefinition = "ltree")
    private String path;

    @Column(name = "is_leaf", nullable = false)
    @Builder.Default
    private boolean leaf = false;

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private Set<JobCategory> children = new HashSet<>();
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/JobCategory.java
git commit -m "feat: create JobCategory entity with LTREE path support"
```

---

### Task 15: Create candidate detail entities (WorkExperience, School, Education, Resume, Position)

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CandidateWorkExperience.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/School.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CandidateEducation.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CandidateResume.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/CandidatePosition.java`
- Create: `src/main/java/org/toanehihi/botcv/domain/model/ids/CandidatePositionId.java`

- [ ] **Step 1: Create CandidateWorkExperience.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "candidate_work_experiences")
public class CandidateWorkExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "job_title", length = 150)
    private String jobTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = false;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
```

- [ ] **Step 2: Create School.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "schools")
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;
}
```

- [ ] **Step 3: Create CandidateEducation.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "candidate_educations")
public class CandidateEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(name = "degree", length = 150)
    private String degree;

    @Column(name = "field_of_study", length = 150)
    private String fieldOfStudy;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = false;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
```

- [ ] **Step 4: Create CandidateResume.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "candidate_resumes")
public class CandidateResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "title", length = 100)
    private String title;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;
}
```

- [ ] **Step 5: Create CandidatePositionId.java**

```java
package org.toanehihi.botcv.domain.model.ids;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class CandidatePositionId implements Serializable {
    private Long candidate;
    private Long category;
}
```

- [ ] **Step 6: Create CandidatePosition.java**

```java
package org.toanehihi.botcv.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.toanehihi.botcv.domain.model.ids.CandidatePositionId;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"candidate", "category"})
@Entity
@Table(name = "candidate_positions")
@IdClass(CandidatePositionId.class)
public class CandidatePosition {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private JobCategory category;
}
```

- [ ] **Step 7: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/CandidateWorkExperience.java \
       src/main/java/org/toanehihi/botcv/domain/model/School.java \
       src/main/java/org/toanehihi/botcv/domain/model/CandidateEducation.java \
       src/main/java/org/toanehihi/botcv/domain/model/CandidateResume.java \
       src/main/java/org/toanehihi/botcv/domain/model/CandidatePosition.java \
       src/main/java/org/toanehihi/botcv/domain/model/ids/CandidatePositionId.java
git commit -m "feat: create candidate detail entities (WorkExperience, School, Education, Resume, Position)"
```

---

### Task 16: Add Permission ManyToMany to Role entity

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/model/Role.java`

- [ ] **Step 1: Add permissions field to Role**

Add import for `Set`, `HashSet`, `Permission`. Add the ManyToMany relationship:

After the `dateCreated` field, add:

```java
@ManyToMany
@JoinTable(name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"))
@Builder.Default
private Set<Permission> permissions = new HashSet<>();
```

The full Role.java should now be:

```java
package org.toanehihi.botcv.domain.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "roles")
public class Role implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @CreationTimestamp
    @Column(name = "date_created")
    private OffsetDateTime dateCreated;

    @ManyToMany
    @JoinTable(name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"))
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
```

- [ ] **Step 2: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/model/Role.java
git commit -m "feat: add Permission ManyToMany to Role entity"
```

---

### Task 17: Create all new repositories

**Files:**
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/PermissionRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/ModeratorRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyVerificationRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyVerificationAttachmentRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateWorkExperienceRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/SchoolRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateEducationRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateResumeRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobCategoryRepository.java`
- Create: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidatePositionRepository.java`

- [ ] **Step 1: Create PermissionRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Permission;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
}
```

- [ ] **Step 2: Create ModeratorRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Moderator;

import java.util.Optional;

@Repository
public interface ModeratorRepository extends JpaRepository<Moderator, Long> {
    Optional<Moderator> findByAccountId(Long accountId);
}
```

- [ ] **Step 3: Create CompanyVerificationRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CompanyVerification;
import org.toanehihi.botcv.domain.model.enums.VerificationStatus;

@Repository
public interface CompanyVerificationRepository extends JpaRepository<CompanyVerification, Long> {
    Page<CompanyVerification> findByStatus(VerificationStatus status, Pageable pageable);
    Page<CompanyVerification> findByCompanyId(Long companyId, Pageable pageable);
}
```

- [ ] **Step 4: Create CompanyVerificationAttachmentRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CompanyVerificationAttachment;
import org.toanehihi.botcv.domain.model.ids.CompanyVerificationAttachmentId;

import java.util.List;

@Repository
public interface CompanyVerificationAttachmentRepository extends JpaRepository<CompanyVerificationAttachment, CompanyVerificationAttachmentId> {
    List<CompanyVerificationAttachment> findByVerificationId(Long verificationId);
}
```

- [ ] **Step 5: Create CandidateWorkExperienceRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidateWorkExperience;

import java.util.List;

@Repository
public interface CandidateWorkExperienceRepository extends JpaRepository<CandidateWorkExperience, Long> {
    List<CandidateWorkExperience> findByCandidateId(Long candidateId);
}
```

- [ ] **Step 6: Create SchoolRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.School;

import java.util.Optional;

@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByName(String name);
}
```

- [ ] **Step 7: Create CandidateEducationRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidateEducation;

import java.util.List;

@Repository
public interface CandidateEducationRepository extends JpaRepository<CandidateEducation, Long> {
    List<CandidateEducation> findByCandidateId(Long candidateId);
}
```

- [ ] **Step 8: Create CandidateResumeRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidateResume;

import java.util.List;

@Repository
public interface CandidateResumeRepository extends JpaRepository<CandidateResume, Long> {
    List<CandidateResume> findByCandidateId(Long candidateId);
}
```

- [ ] **Step 9: Create JobCategoryRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.JobCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobCategoryRepository extends JpaRepository<JobCategory, Long> {
    Optional<JobCategory> findBySlug(String slug);
    List<JobCategory> findByParentId(Long parentId);
    List<JobCategory> findByParentIsNull();
}
```

- [ ] **Step 10: Create CandidatePositionRepository.java**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.CandidatePosition;
import org.toanehihi.botcv.domain.model.ids.CandidatePositionId;

import java.util.List;

@Repository
public interface CandidatePositionRepository extends JpaRepository<CandidatePosition, CandidatePositionId> {
    List<CandidatePosition> findByCandidateId(Long candidateId);
}
```

- [ ] **Step 11: Commit**

```
git add src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/PermissionRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/ModeratorRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyVerificationRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyVerificationAttachmentRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateWorkExperienceRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/SchoolRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateEducationRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidateResumeRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobCategoryRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CandidatePositionRepository.java
git commit -m "feat: create repositories for all new entities"
```

---

## Phase C: Remove Obsolete + Rewire Services

### Task 18: Delete obsolete entities and repositories

**Files to delete:**
- `src/main/java/org/toanehihi/botcv/domain/model/JobFamily.java`
- `src/main/java/org/toanehihi/botcv/domain/model/SubFamily.java`
- `src/main/java/org/toanehihi/botcv/domain/model/JobRole.java`
- `src/main/java/org/toanehihi/botcv/domain/model/AttestationResource.java`
- `src/main/java/org/toanehihi/botcv/domain/model/ids/AttestationResourceId.java`
- `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/AttestationResourceRepository.java`
- `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobFamilyRepository.java`
- `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/SubFamilyRepository.java`
- `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobRoleRepository.java`

- [ ] **Step 1: Delete all obsolete files**

```
git rm src/main/java/org/toanehihi/botcv/domain/model/JobFamily.java \
      src/main/java/org/toanehihi/botcv/domain/model/SubFamily.java \
      src/main/java/org/toanehihi/botcv/domain/model/JobRole.java \
      src/main/java/org/toanehihi/botcv/domain/model/AttestationResource.java \
      src/main/java/org/toanehihi/botcv/domain/model/ids/AttestationResourceId.java \
      src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/AttestationResourceRepository.java \
      src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobFamilyRepository.java \
      src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/SubFamilyRepository.java \
      src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobRoleRepository.java
```

- [ ] **Step 2: Commit**

```
git commit -m "chore: delete obsolete entities (JobFamily, SubFamily, JobRole, AttestationResource) and their repositories"
```

---

### Task 19: Rewire ResourceMapper, ResourceResponse, and CloudinaryStorageImpl

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/resource/ResourceResponse.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/resource/ResourceMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/application/cloud/service/CloudinaryStorageImpl.java`
- Modify: `src/main/java/org/toanehihi/botcv/application/cloud/service/CloudStorageService.java`

- [ ] **Step 1: Update ResourceResponse — remove mimeType/url, add size**

```java
package org.toanehihi.botcv.interfaces.web.dtos.resource;

import java.time.OffsetDateTime;

import org.toanehihi.botcv.domain.model.enums.ResourceType;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceResponse {
    private Long id;
    private ResourceType resourceType;
    private String contentType;
    private String publicId;
    private Long size;
    private String name;
    private String url;
    private OffsetDateTime uploadedAt;
}
```

Note: `url` is kept in the DTO — it will be reconstructed from `publicId` by the mapper, not stored in the DB.

- [ ] **Step 2: Update ResourceMapper — reconstruct URL from publicId**

```java
package org.toanehihi.botcv.infrastructure.persistence.mappers.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Resource;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;

@Component
public class ResourceMapper {

    @Value("${app.cloudinary.base-url:}")
    private String cloudinaryBaseUrl;

    public ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .resourceType(resource.getResourceType())
                .contentType(resource.getContentType())
                .publicId(resource.getPublicId())
                .size(resource.getSize())
                .name(resource.getName())
                .url(buildUrl(resource.getPublicId()))
                .uploadedAt(resource.getUploadedAt())
                .build();
    }

    private String buildUrl(String publicId) {
        if (publicId == null || cloudinaryBaseUrl == null || cloudinaryBaseUrl.isBlank()) {
            return null;
        }
        return cloudinaryBaseUrl + "/" + publicId;
    }
}
```

Note: Add `app.cloudinary.base-url` to application.yml/properties. Value format: `https://res.cloudinary.com/<cloud-name>/raw/upload` (the exact pattern depends on the Cloudinary configuration). If the property is not set, `url` will be null.

- [ ] **Step 3: Update CloudinaryFileInfo record — remove url and mimeType**

In `CloudinaryStorageImpl.java`, replace the record and the `storeFile` method:

The `CloudinaryFileInfo` record changes from:
```java
public record CloudinaryFileInfo(String url, String publicId, String mimeType, String contentType, String fileName) {}
```

To:
```java
public record CloudinaryFileInfo(String publicId, String contentType, long size, String fileName) {}
```

And `storeFile` returns it without `url`/`mimeType`:

```java
@Override
@Transactional
public CloudinaryFileInfo storeFile(MultipartFile file, String folderName) {
    try {
        if (file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        String contentType = file.getContentType();
        String resourceType = detectResourceType(contentType);

        if (contentType == null || !ALLOWED_TYPES.contains(resourceType)) {
            throw new AppException(ErrorCode.FILE_TYPE_NOT_SUPPORTED);
        }

        String originalFileName = file.getOriginalFilename();
        String publicId = UUID.randomUUID().toString();

        @SuppressWarnings("unchecked")
        Map<String, Object> params = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "bot-cv/" + folderName,
                "overwrite", true,
                "resource_type", resourceType);

        @SuppressWarnings("unchecked")
        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

        String uploadedPublicId = (String) uploadResult.get("public_id");
        long fileSize = file.getSize();

        return new CloudinaryFileInfo(uploadedPublicId, contentType, fileSize, originalFileName);
    } catch (IOException e) {
        log.error("Failed to upload file: {}", e.getMessage());
        throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
    }
}
```

- [ ] **Step 4: Update CloudStorageService interface — change downloadFile parameter**

The `downloadFile` method currently takes a `resourceUrl`. Since `url` is removed from the entity, it should take a `publicId` and reconstruct the URL internally. However, this is a larger change — for now, keep the method signature but update the caller to pass the reconstructed URL. The `CloudStorageService.downloadFile(String resourceUrl)` signature stays the same.

- [ ] **Step 5: Commit**

```
git add src/main/java/org/toanehihi/botcv/interfaces/web/dtos/resource/ResourceResponse.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/resource/ResourceMapper.java \
       src/main/java/org/toanehihi/botcv/application/cloud/service/CloudinaryStorageImpl.java
git commit -m "fix: rewire Resource layer — remove url/mimeType, reconstruct URLs from publicId"
```

---

### Task 20: Rewire ResourceServiceImpl

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/application/resource/service/ResourceServiceImpl.java`

- [ ] **Step 1: Rewrite ResourceServiceImpl**

Key changes:
- Remove all `AttestationResource`/`AttestationResourceRepository` references
- Remove `uploadAttestation` method entirely (will be replaced by CompanyVerification flow later)
- All `Resource.builder()` calls: remove `.mimeType()`, `.url()`, `.ownerId()`; add `.size()`
- Replace `ResourceType.AVATAR` → `ResourceType.IMAGE`
- Replace `ResourceType.CV` → `ResourceType.DOCUMENT`
- Replace `ResourceType.COMPANY_LOGO` → `ResourceType.IMAGE`
- Replace `ResourceType.ATTESTATION` → `ResourceType.DOCUMENT`
- In `analyzeResume`: `cloudStorageService.downloadFile(resource.getUrl())` needs to reconstruct URL from `publicId`

```java
package org.toanehihi.botcv.application.resource.service;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.toanehihi.botcv.application.candidate.service.CandidateService;
import org.toanehihi.botcv.application.cloud.service.CloudStorageService;
import org.toanehihi.botcv.application.cloud.service.CloudinaryStorageImpl.CloudinaryFileInfo;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.*;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.domain.model.enums.RoleName;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CandidateRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.RecruiterRepository;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.interfaces.web.dtos.resource.FileData;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResumeAnalysisResponse;

import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.LongConsumer;

@Service
@RequiredArgsConstructor
public class ResourceServiceImpl implements ResourceService {
    private final RecruiterRepository recruiterRepository;
    private final CandidateRepository candidateRepository;
    private final ResourceRepository resourceRepository;
    private final CompanyRepository companyRepository;
    private final ResourceMapper resourceMapper;
    private final CloudStorageService cloudStorageService;
    private final CandidateService candidateService;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.ner-service-url}")
    private String nerServiceUrl;

    @Value("${app.cloudinary.base-url:}")
    private String cloudinaryBaseUrl;

    @Override
    public ResourceResponse updateUserAvatar(Account account, MultipartFile avatar) {
        RoleName role = RoleName.valueOf(account.getRole().getName());

        return switch (role) {
            case RECRUITER -> {
                Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));
                yield updateAvatar(
                        recruiter.getAvatarResourceId(),
                        avatar,
                        resourceId -> {
                            recruiter.setAvatarResourceId(resourceId);
                            recruiter.setDateUpdated(OffsetDateTime.now());
                            recruiterRepository.save(recruiter);
                        });
            }
            case CANDIDATE -> {
                Candidate candidate = candidateRepository.findByAccountId(account.getId())
                        .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));
                yield updateAvatar(
                        candidate.getAvatarResourceId(),
                        avatar,
                        resourceId -> {
                            candidate.setAvatarResourceId(resourceId);
                            candidate.setDateUpdated(OffsetDateTime.now());
                            candidateRepository.save(candidate);
                        });
            }
            default -> throw new AppException(ErrorCode.ACCOUNT_DOES_NOT_SUPPORT);
        };
    }

    private ResourceResponse updateAvatar(
            Long currentAvatarId,
            MultipartFile avatar,
            LongConsumer updateEntity) {

        Optional<Resource> currentAvatar = resourceRepository
                .findByIdAndResourceType(currentAvatarId, ResourceType.IMAGE);

        currentAvatar.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });

        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(avatar, "avatar");

        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.IMAGE)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();

        Resource savedResource = resourceRepository.save(resource);
        updateEntity.accept(savedResource.getId());

        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public ResourceResponse updateCompanyLogo(Account account, MultipartFile logo) {
        Recruiter recruiter = recruiterRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_RECRUITER_NOT_FOUND));

        Company company = recruiter.getCompany();
        if (company == null) {
            throw new AppException(ErrorCode.RECRUITER_COMPANY_NOT_FOUND);
        }

        Optional<Resource> currentLogo = resourceRepository
                .findByIdAndResourceType(company.getLogoResourceId(), ResourceType.IMAGE);

        currentLogo.ifPresent(resource -> {
            resourceRepository.delete(resource);
            cloudStorageService.deleteFile(resource.getPublicId());
        });
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(logo, "company_logo");
        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.IMAGE)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        company.setLogoResourceId(savedResource.getId());
        companyRepository.save(company);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    public ResourceResponse uploadResume(Account account, MultipartFile file) {
        Candidate candidate = candidateRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));
        CloudinaryFileInfo fileInfo = cloudStorageService.storeFile(file, "resume");
        Resource resource = Resource.builder()
                .contentType(fileInfo.contentType())
                .resourceType(ResourceType.DOCUMENT)
                .publicId(fileInfo.publicId())
                .size(fileInfo.size())
                .name(fileInfo.fileName())
                .build();
        Resource savedResource = resourceRepository.save(resource);
        return resourceMapper.toResponse(savedResource);
    }

    @Override
    @Transactional
    public ResumeAnalysisResponse analyzeResume(Long resourceId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND));
        if (resource.getResourceType() != ResourceType.DOCUMENT) {
            throw new AppException(ErrorCode.RESOURCE_TYPE_NOT_ALLOWED);
        }
        String resourceUrl = buildResourceUrl(resource.getPublicId());
        FileData fileData = cloudStorageService.downloadFile(resourceUrl);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(fileData.getContent()) {
            @Override
            public String getFilename() {
                return resource.getName();
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<NerExtractResponse> response = restTemplate.postForEntity(
                nerServiceUrl + "/extract",
                requestEntity,
                NerExtractResponse.class);

        NerExtractResponse wrapper = response.getBody();
        ResumeAnalysisResponse analysisResult = (wrapper != null) ? wrapper.entities : null;

        if (analysisResult != null) {
            Candidate candidate = candidateRepository.findById(resourceId)
                    .orElse(null);
            if (candidate != null) {
                candidateService.updateProfileFromCV(candidate.getAccount().getId(), analysisResult);
            }
        }
        return analysisResult;
    }

    private String buildResourceUrl(String publicId) {
        if (publicId == null) return null;
        if (cloudinaryBaseUrl != null && !cloudinaryBaseUrl.isBlank()) {
            return cloudinaryBaseUrl + "/" + publicId;
        }
        return publicId;
    }

    public record NerExtractResponse(ResumeAnalysisResponse entities) {
    }
}
```

Note: The `uploadAttestation` method is removed. The `ResourceService` interface must also have its `uploadAttestation` method removed (and any controller endpoint calling it updated). Also note `analyzeResume` no longer uses `resource.getOwnerId()` — instead it takes a different approach to find the candidate. This may need to be refined based on how the caller provides context.

- [ ] **Step 2: Update ResourceService interface — remove uploadAttestation signature**

Remove the `uploadAttestation` method from the `ResourceService` interface. The exact interface file path and content depends on the current interface — find and remove the method.

- [ ] **Step 3: Commit**

```
git add src/main/java/org/toanehihi/botcv/application/resource/service/ResourceServiceImpl.java \
       src/main/java/org/toanehihi/botcv/application/resource/service/ResourceService.java
git commit -m "fix: rewire ResourceServiceImpl — remove attestation flow, update ResourceType values, remove url/mimeType"
```

---

### Task 21: Rewire CandidateServiceImpl and CandidateMapper

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/application/candidate/service/CandidateServiceImpl.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/candidate/CandidateMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/candidate/CandidateRequest.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/candidate/CandidateResponse.java`

- [ ] **Step 1: Update CandidateRequest DTO**

Remove `seniority`, `salaryExpectMin`, `salaryExpectMax`. Add `experienceYears`, `salaryExpect`.

```java
package org.toanehihi.botcv.interfaces.web.dtos.candidate;

import java.util.Set;

import org.toanehihi.botcv.domain.model.enums.ExperienceYears;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationRequest;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillRequest;

import lombok.Getter;

@Getter
public class CandidateRequest {
    private String fullName;
    private String phone;
    private String email;
    private LocationRequest location;
    private ExperienceYears experienceYears;
    private Integer salaryExpect;
    private String currency;
    private Boolean remotePref;
    private Boolean relocationPref;
    private String bio;
    private Set<CandidateSkillRequest> skills;
}
```

- [ ] **Step 2: Update CandidateResponse DTO**

Remove `seniority`, `salaryExpectMin`, `salaryExpectMax`. Add `experienceYears`, `salaryExpect`.

```java
package org.toanehihi.botcv.interfaces.web.dtos.candidate;

import java.time.OffsetDateTime;
import java.util.Set;

import org.toanehihi.botcv.domain.model.enums.ExperienceYears;
import org.toanehihi.botcv.interfaces.web.dtos.location.LocationResponse;
import org.toanehihi.botcv.interfaces.web.dtos.resource.ResourceResponse;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillResponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateResponse {
    private Long id;
    private Long accountId;
    private String fullName;
    private String phone;
    private String email;
    private LocationResponse location;
    private ExperienceYears experienceYears;
    private Integer salaryExpect;
    private String currency;
    private Boolean remotePref;
    private Boolean relocationPref;
    private ResourceResponse resource;
    private String bio;
    private OffsetDateTime dateCreated;
    private OffsetDateTime dateUpdated;
    private Set<CandidateSkillResponse> skills;
}
```

- [ ] **Step 3: Update CandidateMapper**

Update `updateCandidate` — replace `setSeniority`/`setSalaryExpectMin`/`setSalaryExpectMax` with `setExperienceYears`/`setSalaryExpect`.
Update `toResponse` — replace field mappings, change `ResourceType.AVATAR` → `ResourceType.IMAGE`.

```java
package org.toanehihi.botcv.infrastructure.persistence.mappers.candidate;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.Candidate;
import org.toanehihi.botcv.domain.model.enums.ResourceType;
import org.toanehihi.botcv.infrastructure.persistence.mappers.location.LocationMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.resource.ResourceMapper;
import org.toanehihi.botcv.infrastructure.persistence.mappers.skill.CandidateSkillMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.ResourceRepository;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateRequest;
import org.toanehihi.botcv.interfaces.web.dtos.candidate.CandidateResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidateMapper {
    private final LocationMapper locationMapper;
    private final CandidateSkillMapper candidateSkillMapper;
    private final ResourceRepository resourceRepository;
    private final ResourceMapper resourceMapper;

    public void updateCandidate(Candidate candidate, CandidateRequest request) {
        candidate.setFullName(request.getFullName());
        candidate.setPhone(request.getPhone());
        candidate.setExperienceYears(request.getExperienceYears());
        candidate.setSalaryExpect(request.getSalaryExpect());
        candidate.setCurrency(request.getCurrency());
        candidate.setRemotePref(request.getRemotePref());
        candidate.setRelocationPref(request.getRelocationPref());
        candidate.setBio(request.getBio());
    }

    public CandidateResponse toResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId())
                .accountId(candidate.getAccount().getId())
                .fullName(candidate.getFullName())
                .phone(candidate.getPhone())
                .location(candidate.getLocation() != null ? locationMapper.toResponse(candidate.getLocation()) : null)
                .experienceYears(candidate.getExperienceYears())
                .salaryExpect(candidate.getSalaryExpect())
                .currency(candidate.getCurrency())
                .remotePref(candidate.getRemotePref())
                .relocationPref(candidate.getRelocationPref())
                .email(candidate.getAccount().getEmail())
                .resource(candidate.getAvatarResourceId() != null
                        ? resourceRepository.findByIdAndResourceType(candidate.getAvatarResourceId(), ResourceType.IMAGE)
                                .map(resourceMapper::toResponse)
                                .orElse(null)
                        : null)
                .bio(candidate.getBio())
                .dateCreated(candidate.getDateCreated())
                .dateUpdated(candidate.getDateUpdated())
                .skills(candidate.getSkills().stream().map(candidateSkillMapper::toResponse)
                        .collect(Collectors.toSet()))
                .build();
    }
}
```

- [ ] **Step 4: Update CandidateServiceImpl — remove seniority/calculateSeniority, fix skill level**

Key changes:
- Remove `calculateSeniority()` private method
- In `updateProfileFromCV()`: remove `candidate.setSeniority()` calls, remove seniority calculation logic
- In `updateCandidateSkills()`: remove `candidateSkill.setLevel(1)` (level field no longer exists)
- In `applyJob()`: update Resource builder — remove `.mimeType()`, `.url()`, add `.size()`; change `ResourceType.CV` → `ResourceType.DOCUMENT`
- In `getCandidateResumes()`: change `ResourceType.CV` → `ResourceType.DOCUMENT`

The `updateProfileFromCV` method should be simplified to only update skills and bio (no seniority):

```java
@Override
public void updateProfileFromCV(Long accountId, ResumeAnalysisResponse cvData) {
    Candidate candidate = candidateRepository.findByAccountId(accountId)
            .orElseThrow(() -> new AppException(ErrorCode.ACCOUNT_CANDIDATE_NOT_FOUND));

    if (cvData.getSkills() != null && !cvData.getSkills().isEmpty()) {
        updateCandidateSkills(candidate, cvData.getSkills());
    }

    String extractedInfo = buildBioFromCV(cvData);
    if (!extractedInfo.isEmpty()) {
        String currentBio = candidate.getBio();

        if (currentBio == null || currentBio.isBlank()) {
            candidate.setBio(extractedInfo);
        } else {
            String normCurrent = currentBio.replaceAll("\\s+", "").toLowerCase();
            String normNew = extractedInfo.replaceAll("\\s+", "").toLowerCase();
            if (!normCurrent.contains(normNew)) {
                candidate.setBio(currentBio + "\n\n" + extractedInfo);
            }
        }
    }

    candidateRepository.save(candidate);
}
```

In `updateCandidateSkills`, remove the `candidateSkill.setLevel(1)` line:

```java
CandidateSkill candidateSkill = new CandidateSkill();
candidateSkill.setCandidate(candidate);
candidateSkill.setSkill(skill);
```

In `applyJob`, update the Resource builder:

```java
Resource resource = Resource.builder()
        .contentType(fileInfo.contentType())
        .resourceType(ResourceType.DOCUMENT)
        .publicId(fileInfo.publicId())
        .size(fileInfo.size())
        .name(fileInfo.fileName())
        .build();
```

In `updateProfile`, remove `candidateSkill.setLevel(skillRequest.getLevel())` — the `CandidateSkill` builder call becomes:

```java
CandidateSkill candidateSkill = CandidateSkill.builder()
        .candidate(candidate)
        .skill(skill)
        .build();
```

In `getCandidateResumes`, this method used `ResourceType.CV` with `resourceRepository.findByOwnerIdAndResourceType` — since `ownerId` is removed from Resource, this method needs to be reworked. For now, remove it or replace with a TODO comment. The new `CandidateResume` entity will be used instead in a future task.

- [ ] **Step 5: Commit**

```
git add src/main/java/org/toanehihi/botcv/interfaces/web/dtos/candidate/CandidateRequest.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/candidate/CandidateResponse.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/candidate/CandidateMapper.java \
       src/main/java/org/toanehihi/botcv/application/candidate/service/CandidateServiceImpl.java
git commit -m "fix: rewire Candidate layer — experienceYears/salaryExpect, remove seniority, update ResourceType"
```

---

### Task 22: Rewire CandidateSkillMapper/DTOs and CompanyMapper/DTOs

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/skill/CandidateSkillMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/skill/CandidateSkillResponse.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/skill/CandidateSkillRequest.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/company/CompanyMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/company/CompanyResponse.java`

- [ ] **Step 1: Remove level from CandidateSkillResponse**

```java
package org.toanehihi.botcv.interfaces.web.dtos.skill;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CandidateSkillResponse {
    private SkillResponse skill;
}
```

- [ ] **Step 2: Remove level from CandidateSkillRequest**

```java
package org.toanehihi.botcv.interfaces.web.dtos.skill;

import lombok.Getter;

@Getter
public class CandidateSkillRequest {
    private String skillName;
}
```

- [ ] **Step 3: Update CandidateSkillMapper — remove level**

```java
package org.toanehihi.botcv.infrastructure.persistence.mappers.skill;

import org.springframework.stereotype.Component;
import org.toanehihi.botcv.domain.model.CandidateSkill;
import org.toanehihi.botcv.interfaces.web.dtos.skill.CandidateSkillResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CandidateSkillMapper {
    private final SkillMapper skillMapper;

    public CandidateSkillResponse toResponse(CandidateSkill candidateSkill) {
        return CandidateSkillResponse.builder()
                .skill(skillMapper.toResponse(candidateSkill.getSkill()))
                .build();
    }
}
```

- [ ] **Step 4: Update CompanyMapper — fix size type, change ResourceType, remove attestations reference**

In `updateCompany`: `company.setSize(request.getWebsite())` is a bug — should be `company.setSize(request.getSize())`. But now `size` is `CompanySize` enum, so parse it:

```java
public void updateCompany(Company company, CompanyRequest request) {
    company.setName(request.getName());
    company.setWebsite(request.getWebsite());
    if (request.getSize() != null) {
        company.setSize(CompanySize.valueOf(request.getSize()));
    }
    company.setDescription(request.getDescription());
    company.setPhone(request.getPhone());
    company.setEmail(request.getEmail());
    company.setIndustry(request.getIndustry());
}
```

In `toResponse`: change `ResourceType.COMPANY_LOGO` → `ResourceType.IMAGE`:

```java
.resource(resourceRepository.findByIdAndResourceType(company.getLogoResourceId(), ResourceType.IMAGE)
        .map(resourceMapper::toResponse)
        .orElse(null))
```

Add import for `CompanySize`:
```java
import org.toanehihi.botcv.domain.model.enums.CompanySize;
```

- [ ] **Step 5: Update CompanyResponse — change size type to String (keep API compatible)**

The `size` field stays as `String` in the DTO for API compatibility. The mapper will convert `CompanySize.name()`:

In CompanyMapper `toResponse`:
```java
.size(company.getSize() != null ? company.getSize().name() : null)
```

- [ ] **Step 6: Commit**

```
git add src/main/java/org/toanehihi/botcv/interfaces/web/dtos/skill/CandidateSkillResponse.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/skill/CandidateSkillRequest.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/skill/CandidateSkillMapper.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/company/CompanyMapper.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/company/CompanyResponse.java
git commit -m "fix: rewire CandidateSkill (remove level) and CompanyMapper (CompanySize, ResourceType.IMAGE)"
```

---

### Task 23: Rewire CompanyServiceImpl — replace attestation flow with verification flow

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/application/company/service/CompanyServiceImpl.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyRepository.java`

- [ ] **Step 1: Rewrite CompanyServiceImpl**

Remove all `AttestationResource`/`AttestationResourceRepository` references. The verification flow will now use `CompanyVerification` + `CompanyVerificationAttachment`. For now, simplify the verify method to work without attestations, and update the unverified companies query.

```java
package org.toanehihi.botcv.application.company.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.toanehihi.botcv.application.email.service.EmailService;
import org.toanehihi.botcv.domain.exception.AppException;
import org.toanehihi.botcv.domain.exception.ErrorCode;
import org.toanehihi.botcv.domain.model.Account;
import org.toanehihi.botcv.domain.model.Company;
import org.toanehihi.botcv.infrastructure.persistence.mappers.company.CompanyMapper;
import org.toanehihi.botcv.infrastructure.persistence.repositories.CompanyRepository;
import org.toanehihi.botcv.interfaces.web.dtos.PageResult;
import org.toanehihi.botcv.interfaces.web.dtos.company.CompanyResponse;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyRequest;
import org.toanehihi.botcv.interfaces.web.dtos.company.VerifyCompanyResponse;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public VerifyCompanyResponse verifyAttestation(Account account, VerifyCompanyRequest request) {
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));

        company.setVerified(request.isApproved());
        Company savedCompany = companyRepository.save(company);

        // Notify the first recruiter of the company
        company.getRecruiters().stream().findFirst().ifPresent(recruiter ->
                emailService.sendCompanyVerificationResult(
                        recruiter.getAccount().getEmail(),
                        request.isApproved(),
                        request.isApproved() ? null : request.getReason()));

        return VerifyCompanyResponse.builder()
                .companyId(savedCompany.getId())
                .approved(request.isApproved())
                .reason(request.getReason())
                .build();
    }

    @Override
    public PageResult<CompanyResponse> getVerifyList(Account account, int page, int size, String sortBy,
            String sortDir) {
        Sort sort = Sort.by(sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Company> unverifiedCompaniesPage = companyRepository.findByVerifiedFalse(pageable);
        return PageResult.from(unverifiedCompaniesPage.map(companyMapper::toResponse));
    }

    @Override
    public CompanyResponse getCompanyInfo(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new AppException(ErrorCode.COMPANY_NOT_FOUND));
        return companyMapper.toResponse(company);
    }
}
```

Note: Removed `getCompanyAttestations` method — this will be replaced by a CompanyVerification-based method later. The `CompanyService` interface must also have this method removed.

- [ ] **Step 2: Update CompanyRepository — replace attestation-based queries**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Page<Company> findByVerifiedFalse(Pageable pageable);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.verified = false")
    Long countUnverifiedCompanies();
}
```

Note: Removed `findByRecruiter` (Company no longer has a single recruiter reference).

- [ ] **Step 3: Commit**

```
git add src/main/java/org/toanehihi/botcv/application/company/service/CompanyServiceImpl.java \
       src/main/java/org/toanehihi/botcv/application/company/service/CompanyService.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/CompanyRepository.java
git commit -m "fix: rewire CompanyService — remove attestation flow, simplify to verification-ready"
```

---

### Task 24: Rewire JobServiceImpl, JobMapper, and Job DTOs

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/application/job/service/JobServiceImpl.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/job/JobMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/CreateJobRequest.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/UpdateJobRequest.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobResponse.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobDetailResponse.java`
- Modify: `src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobEventPayload.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobRepository.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/job/JobApplicationMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/recruiter/RecruiterMapper.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/ResourceRepository.java`

- [ ] **Step 1: Update CreateJobRequest — replace jobRoleId with categoryId**

```java
package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Getter;
import org.toanehihi.botcv.domain.model.enums.EmploymentType;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class CreateJobRequest {
    private String title;
    private Long categoryId;
    private SeniorityLevel seniorityLevel;
    private EmploymentType employmentType;
    private int minExperienceYears;
    private Long locationId;
    private WorkMode workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private Integer maxCandidates;
    private OffsetDateTime dateExpires;
    private String summary;
    private String responsibilities;
    private String requirements;
    private String niceToHave;
    private String benefits;
    private String hiringProcess;
    private String notes;
    private boolean saveAsDraft;
    private List<String> skills;
}
```

- [ ] **Step 2: Update UpdateJobRequest — replace jobRoleId with categoryId**

```java
package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Getter;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class UpdateJobRequest {
    private String title;
    private Long categoryId;
    private String seniorityLevel;
    private String employmentType;
    private Integer minExperienceYears;
    private Long locationId;
    private String workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private OffsetDateTime dateExpires;
    private String summary;
    private String responsibilities;
    private String requirements;
    private String niceToHave;
    private String benefits;
    private String hiringProcess;
    private String notes;
    private List<String> skills;
}
```

- [ ] **Step 3: Update JobResponse — replace jobRole with category**

Replace `String jobRole` with `String category`. Replace `String logo` with reconstructed URL approach:

```java
package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Builder;
import lombok.Getter;
import org.toanehihi.botcv.domain.model.enums.JobStatus;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;
import org.toanehihi.botcv.interfaces.web.dtos.skill.SkillResponse;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
@Getter
public class JobResponse {
    private Long id;
    private String title;
    private String company;
    private String logo;
    private String category;
    private SeniorityLevel seniority;
    private int minExperienceYears;
    private String location;
    private WorkMode workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private Integer maxCandidates;
    private OffsetDateTime datePosted;
    private OffsetDateTime dateExpires;
    private JobStatus status;
    private List<SkillResponse> skills;
}
```

- [ ] **Step 4: Update JobDetailResponse — replace jobRole with category**

Replace `String jobRole` with `String category`:

```java
package org.toanehihi.botcv.interfaces.web.dtos.job;

import lombok.Builder;
import lombok.Getter;
import org.toanehihi.botcv.domain.model.enums.JobStatus;
import org.toanehihi.botcv.domain.model.enums.SeniorityLevel;
import org.toanehihi.botcv.domain.model.enums.WorkMode;
import org.toanehihi.botcv.interfaces.web.dtos.skill.SkillResponse;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class JobDetailResponse {
    private Long id;
    private String title;
    private String company;
    private String category;
    private SeniorityLevel seniority;
    private int minExperienceYears;
    private String location;
    private WorkMode workMode;
    private Integer salaryMin;
    private Integer salaryMax;
    private String currency;
    private Integer maxCandidates;
    private OffsetDateTime datePosted;
    private OffsetDateTime dateExpires;
    private JobStatus status;
    private String summary;
    private String responsibilities;
    private String requirements;
    private String niceToHave;
    private String benefits;
    private String hiringProcess;
    private String notes;
    private List<SkillResponse> skills;
}
```

- [ ] **Step 5: Update JobEventPayload — replace jobRole with category**

Replace `String jobRole` with `String category`.

- [ ] **Step 6: Update JobMapper — replace all jobRole references with category**

All `.jobRole(job.getJobRole().getName())` → `.category(job.getCategory() != null ? job.getCategory().getName() : null)`.
Remove `Resource::getUrl` usage for logo — use ResourceMapper or reconstruct from publicId.
Remove `.jobRole(null)` from `toEntity` — add `.category(null)` instead.

Key method changes:

```java
public JobResponse toResponse(Job job) {
    String companyLogoPublicId = null;
    if (job.getCompany() != null && job.getCompany().getLogoResourceId() != null) {
        companyLogoPublicId = resourceRepository.findById(job.getCompany().getLogoResourceId())
                .map(Resource::getPublicId)
                .orElse(null);
    }
    return JobResponse.builder()
            .id(job.getId())
            .title(job.getTitle())
            .company(job.getCompany().getName())
            .logo(companyLogoPublicId)
            .category(job.getCategory() != null ? job.getCategory().getName() : null)
            .seniority(job.getSeniority())
            .minExperienceYears(job.getMinExperienceYears())
            .location(extractLocation(job.getLocation()))
            .workMode(job.getWorkMode())
            .salaryMin(job.getSalaryMin())
            .salaryMax(job.getSalaryMax())
            .currency(job.getCurrency())
            .datePosted(job.getDatePosted())
            .dateExpires(job.getDateExpires())
            .status(job.getStatus())
            .maxCandidates(job.getMaxCandidates())
            .skills(job.getSkills().stream().map(skillMapper::toResponse).toList())
            .build();
}
```

Similar changes in `toJobDetailResponse` and `toEventPayload`.

In `toEntity`:
```java
public Job toEntity(CreateJobRequest request) {
    return Job.builder()
            .title(request.getTitle())
            .seniority(request.getSeniorityLevel())
            .employmentType(request.getEmploymentType())
            .minExperienceYears(request.getMinExperienceYears())
            .workMode(request.getWorkMode())
            .salaryMin(request.getSalaryMin())
            .salaryMax(request.getSalaryMax())
            .currency(request.getCurrency())
            .maxCandidates(request.getMaxCandidates())
            .dateExpires(request.getDateExpires())
            .description(JobDescription.builder()
                    .summary(request.getSummary())
                    .responsibilities(request.getResponsibilities())
                    .requirements(request.getRequirements())
                    .niceToHave(request.getNiceToHave())
                    .benefits(request.getBenefits())
                    .hiringProcess(request.getHiringProcess())
                    .notes(request.getNotes())
                    .build())
            .build();
}
```

- [ ] **Step 7: Update JobServiceImpl — replace JobRole with JobCategory**

Replace `JobRoleRepository` with `JobCategoryRepository`. In `createJob`:

```java
JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new AppException(ErrorCode.JOB_CATEGORY_NOT_FOUND));
job.setCategory(category);
job.setRecruiter(recruiter);
```

In `updateJobRelations`: replace `jobRoleRepository` with `jobCategoryRepository`:

```java
if (request.getCategoryId() != null) {
    JobCategory category = jobCategoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new AppException(ErrorCode.JOB_CATEGORY_NOT_FOUND));
    job.setCategory(category);
}
```

In `outboxEventService.saveOutboxEvent` calls: change `job.getId()` (Long) to `String.valueOf(job.getId())` (String) — this is the aggregateId type change.

- [ ] **Step 8: Update JobRepository — remove jobRole fetch join**

In `findByIdWithRelations`, replace `left join fetch j.jobRole jr` with `left join fetch j.category cat`:

```java
@Query("select j from Job j" +
        " left join fetch j.skills s" +
        " left join fetch j.category cat" +
        " left join fetch j.location l" +
        " left join fetch j.company c" +
        " where j.id = :id")
Optional<Job> findByIdWithRelations(@NonNull Long id);
```

- [ ] **Step 9: Update JobApplicationMapper — change ResourceType.CV to DOCUMENT, ResourceType.AVATAR to IMAGE**

In `toResponse`: `ResourceType.CV` → `ResourceType.DOCUMENT`
In `toApplicantResponse`: `ResourceType.CV` → `ResourceType.DOCUMENT`, `ResourceType.AVATAR` → `ResourceType.IMAGE`

- [ ] **Step 10: Update RecruiterMapper — change ResourceType.AVATAR to IMAGE**

Change `ResourceType.AVATAR` → `ResourceType.IMAGE`. Also handle null avatarResourceId gracefully:

```java
.resource(recruiter.getAvatarResourceId() != null
        ? resourceRepository.findByIdAndResourceType(
                recruiter.getAvatarResourceId(),
                ResourceType.IMAGE)
                .map(resourceMapper::toResponse)
                .orElse(null)
        : null)
```

- [ ] **Step 11: Update ResourceRepository — remove ownerId-based methods**

Remove `findAllByOwnerIdAndResourceType` and `findByOwnerIdAndResourceType` since `ownerId` no longer exists:

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.Resource;
import org.toanehihi.botcv.domain.model.enums.ResourceType;

import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    Optional<Resource> findByIdAndResourceType(Long id, ResourceType resourceType);
    Optional<Resource> findByPublicId(String publicId);
}
```

- [ ] **Step 12: Commit**

```
git add src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/CreateJobRequest.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/UpdateJobRequest.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobResponse.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobDetailResponse.java \
       src/main/java/org/toanehihi/botcv/interfaces/web/dtos/job/JobEventPayload.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/job/JobMapper.java \
       src/main/java/org/toanehihi/botcv/application/job/service/JobServiceImpl.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/JobRepository.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/job/JobApplicationMapper.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/mappers/recruiter/RecruiterMapper.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/ResourceRepository.java
git commit -m "fix: rewire Job layer — JobCategory replaces JobRole, update all ResourceType values"
```

---

### Task 25: Rewire OutboxEvent services — aggregateId Long → String

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxEventService.java`
- Modify: `src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxEventServiceImpl.java`
- Modify: `src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxReconciliationService.java`
- Modify: `src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/OutboxEventRepository.java`

- [ ] **Step 1: Update OutboxEventService interface — aggregateId Long → String**

```java
package org.toanehihi.botcv.application.outbox.service;

import org.toanehihi.botcv.domain.model.OutboxEvent;

import java.util.UUID;

public interface OutboxEventService {
    OutboxEvent saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload);
    OutboxEvent saveOutboxEvent(String aggregateType, String aggregateId, String eventType, String payload, UUID traceId);
}
```

- [ ] **Step 2: Update OutboxEventServiceImpl — aggregateId Long → String**

Change parameter type from `Long` to `String` in both methods. The builder call already uses `aggregateId` directly.

- [ ] **Step 3: Update OutboxReconciliationService**

Change `Set<Long>` to `Set<String>` for `existingOutboxJobIds`. Change `outboxEventService.saveOutboxEvent(AggregateType.JOB.name(), job.getId(), ...)` to `outboxEventService.saveOutboxEvent(AggregateType.JOB.name(), String.valueOf(job.getId()), ...)`.

Also update the `existingOutboxJobIds.contains(job.getId())` check — since IDs are now Strings, compare with `existingOutboxJobIds.contains(String.valueOf(job.getId()))`.

- [ ] **Step 4: Update OutboxEventRepository — aggregateId type changes**

```java
package org.toanehihi.botcv.infrastructure.persistence.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.toanehihi.botcv.domain.model.OutboxEvent;
import org.toanehihi.botcv.domain.model.enums.OutboxStatus;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Set;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM OutboxEvent e WHERE e.status = :status ORDER BY e.occurredAt ASC")
    List<OutboxEvent> findPendingEvents(@Param("status") OutboxStatus status);

    boolean existsByAggregateTypeAndAggregateId(String aggregateType, String aggregateId);

    @Query("SELECT DISTINCT e.aggregateId FROM OutboxEvent e WHERE e.aggregateType = :aggregateType")
    Set<String> findDistinctAggregateIdsByAggregateType(@Param("aggregateType") String aggregateType);
}
```

- [ ] **Step 5: Update all callers of saveOutboxEvent in JobServiceImpl**

All calls like `outboxEventService.saveOutboxEvent(AggregateType.JOB.name(), job.getId(), ...)` must change `job.getId()` to `String.valueOf(job.getId())`.

- [ ] **Step 6: Commit**

```
git add src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxEventService.java \
       src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxEventServiceImpl.java \
       src/main/java/org/toanehihi/botcv/application/outbox/service/OutboxReconciliationService.java \
       src/main/java/org/toanehihi/botcv/infrastructure/persistence/repositories/OutboxEventRepository.java \
       src/main/java/org/toanehihi/botcv/application/job/service/JobServiceImpl.java
git commit -m "fix: OutboxEvent aggregateId Long → String across service and repository layers"
```

---

## Phase D: Build Verification + Cleanup

### Task 26: Add missing ErrorCode and configuration entries

**Files:**
- Modify: `src/main/java/org/toanehihi/botcv/domain/exception/ErrorCode.java` (if JOB_CATEGORY_NOT_FOUND doesn't exist)
- Modify: `src/main/resources/application.yml` or `application.properties` (add `app.cloudinary.base-url`)

- [ ] **Step 1: Add JOB_CATEGORY_NOT_FOUND to ErrorCode**

Check if `ErrorCode.JOB_CATEGORY_NOT_FOUND` exists. If not, add it following the existing pattern.

- [ ] **Step 2: Add cloudinary base URL configuration**

Add to application.yml/properties:
```yaml
app:
  cloudinary:
    base-url: ${CLOUDINARY_BASE_URL:}
```

- [ ] **Step 3: Commit**

```
git add src/main/java/org/toanehihi/botcv/domain/exception/ErrorCode.java \
       src/main/resources/application.yml
git commit -m "chore: add JOB_CATEGORY_NOT_FOUND error code and cloudinary base URL config"
```

---

### Task 27: Build verification and cleanup

- [ ] **Step 1: Run compilation**

```
mvn clean compile -f pom.xml
```

Expected: BUILD SUCCESS. If there are errors, fix them one by one.

- [ ] **Step 2: Fix any remaining compilation errors**

Common issues to check:
- Unused imports referencing deleted entities (JobFamily, SubFamily, JobRole, AttestationResource)
- References to removed fields (Resource.url, Resource.mimeType, Resource.ownerId, Candidate.seniority, etc.)
- References to removed enum values (ResourceType.AVATAR, ResourceType.CV, ResourceType.COMPANY_LOGO, etc.)
- Type mismatches where `Long aggregateId` was changed to `String`
- StatisticServiceImpl may reference AttestationResource or old company queries

- [ ] **Step 3: Remove unused imports across all modified files**

Run compilation again and fix any remaining import issues.

- [ ] **Step 4: Final verification compile**

```
mvn clean compile -f pom.xml
```

Expected: BUILD SUCCESS

- [ ] **Step 5: Commit cleanup**

```
git add -A
git commit -m "chore: fix compilation errors and remove unused imports after schema alignment"
```

---

## Risk Notes

- **Resource.url removal (Medium):** URLs are now reconstructed from `publicId` via `ResourceMapper.buildUrl()`. The `app.cloudinary.base-url` config property must be set correctly in all environments.

- **Company verification flow rewrite (Medium):** The attestation flow is removed but the full CompanyVerification workflow (submit → review with attachments) is only scaffolded as entities/repos. The CompanyServiceImpl verification is simplified. A follow-up task should build the complete verification submission flow.

- **analyzeResume method (Low-Medium):** The `resource.getOwnerId()` was used to find the candidate. Since `ownerId` is removed, the method needs an alternative way to find the candidate (e.g., pass candidate ID from the caller, or use CandidateResume entity lookup).

- **getCandidateResumes (Low-Medium):** Used `ResourceRepository.findByOwnerIdAndResourceType` which relied on `ownerId`. Must be replaced with `CandidateResumeRepository.findByCandidateId` in a follow-up.

---
