# Schema Alignment Refactoring Design

## Goal

Align all JPA domain entities with the new V1__init.sql database schema. This includes fixing mismatches on existing entities, creating 11 new entities for new tables, removing 4 obsolete entities, and rewiring all affected services, mappers, DTOs, and controllers.

## Context

- Base package: `org.toanehihi.botcv`
- The V1__init.sql is the authoritative schema (final, but adjustable for business logic bugs)
- No tests required
- ResourceType uses generic file types (IMAGE, DOCUMENT, VIDEO) not domain-specific values
- Full implementation: new entities get repositories and are wired into services
- Old taxonomy (JobFamily/SubFamily/JobRole) is replaced by JobCategory LTREE hierarchy

## Architecture

Bottom-up entity-first approach in 4 phases:

1. **Phase A** — Fix existing entity mismatches + new enums
2. **Phase B** — Create new entities + repositories
3. **Phase C** — Remove obsolete entities + rewire services
4. **Phase D** — Build verification + cleanup

Each phase must compile independently before proceeding to the next.

---

## Phase A: Entity Alignment

### A1. Table Name Fixes

| Entity | Current `@Table` | Correct `@Table` |
|--------|-----------------|-------------------|
| `CompanyLocation` | `company_location` | `company_locations` |
| `JobDescription` | `job_description` | `job_descriptions` |

### A2. New Enums

Create in `domain/model/enums/`:

**CompanySize.java:**
```java
public enum CompanySize {
    MICRO, SMALL, MEDIUM, LARGE, ENTERPRISE;
}
```

**ExperienceYears.java:**
```java
public enum ExperienceYears {
    NO_EXPERIENCE, UNDER_1_YEAR, ONE_YEAR, TWO_YEARS,
    THREE_YEARS, FOUR_YEARS, FIVE_YEARS, OVER_5_YEARS;
}
```

**VerificationStatus.java:**
```java
public enum VerificationStatus {
    SUBMITTED, APPROVED, REJECTED;
}
```

**ResourceType.java (replace existing):**
```java
public enum ResourceType {
    IMAGE, DOCUMENT, VIDEO;
}
```

### A3. Resource Entity

Remove fields not in schema, add missing fields:

| Action | Field | Details |
|--------|-------|---------|
| REMOVE | `url` | Not in SQL |
| REMOVE | `mimeType` | Not in SQL |
| REMOVE | `ownerId` | Not in SQL |
| ADD | `size` | `@Column(name = "size", nullable = false) Long size` |
| FIX | `contentType` | Add `@Column(name = "content_type", nullable = false)` |

### A4. Candidate Entity

| Action | Field | Details |
|--------|-------|---------|
| REMOVE | `seniority` | Not in candidates table (only in jobs) |
| REMOVE | `salaryExpectMin` | Not in SQL |
| REMOVE | `salaryExpectMax` | Not in SQL |
| ADD | `salaryExpect` | `@Column(name = "salary_expect") Integer salaryExpect` |
| ADD | `experienceYears` | `@Enumerated(EnumType.STRING) @Column(name = "experience_years", columnDefinition = "experience_years") ExperienceYears experienceYears` |

### A5. CandidateSkill Entity

| Action | Field | Details |
|--------|-------|---------|
| REMOVE | `level` | Not in SQL — table is pure join (candidate_id, skill_id) |

### A6. Company Entity

| Action | Field | Details |
|--------|-------|---------|
| FIX | `verified` | Change `@Column(name = "verified")` to `@Column(name = "is_verified")` |
| FIX | `size` | Change type from `String` to `CompanySize`, add `@Enumerated(EnumType.STRING)` and `columnDefinition = "company_size"` |
| REMOVE | `attestations` | `Set<AttestationResource>` collection — entity being deleted |
| FIX | `recruiter` | Change `@OneToOne(mappedBy = "company") Recruiter recruiter` to `@OneToMany(mappedBy = "company") Set<Recruiter> recruiters` |

### A7. Recruiter Entity

| Action | Field | Details |
|--------|-------|---------|
| FIX | `company` | Change `@OneToOne` to `@ManyToOne` |
| FIX | `avatarResourceId` | Remove `nullable = false` (SQL allows null) |

### A8. Job Entity

| Action | Field | Details |
|--------|-------|---------|
| REMOVE | `jobRole` | `@ManyToOne JobRole` — old taxonomy gone |
| ADD | `category` | `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id") JobCategory category` |
| ADD | `recruiter` | `@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "recruiter_id") Recruiter recruiter` |
| ADD | `dateCreated` | `@CreationTimestamp @Column(name = "date_created") OffsetDateTime dateCreated` |
| ADD | `dateUpdated` | `@UpdateTimestamp @Column(name = "date_updated") OffsetDateTime dateUpdated` |

### A9. JobDescription Entity — Structural PK Fix

SQL uses `job_id` as the PRIMARY KEY (no separate `id` column). The entity currently has a synthetic `@Id @GeneratedValue Long id` plus `@JoinColumn(name = "job_id")`.

Fix: Remove synthetic `id`. Use `@MapsId` pattern:
```java
@Id
private Long id;  // value comes from the associated Job

@OneToOne(fetch = FetchType.LAZY)
@MapsId
@JoinColumn(name = "job_id")
private Job job;
```

### A10. OutboxEvent Entity

| Action | Field | Details |
|--------|-------|---------|
| FIX | `aggregateType` | Add `@Column(name = "aggregate_type", ...)` |
| FIX | `aggregateId` | Change type `Long` to `String`; add `@Column(name = "aggregate_id", ...)` |
| FIX | `eventType` | Add `@Column(name = "event_type", ...)`, change length to 50 |
| ADD | `processedAt` | `@Column(name = "processed_at") OffsetDateTime processedAt` |

### A11. Interview Entity

| Action | Field | Details |
|--------|-------|---------|
| ADD | `meetingUrl` | `@Column(name = "meeting_url", length = 500) String meetingUrl` |
| FIX | `scheduledAt` | Add `@Column(name = "scheduled_at")` |
| FIX | `status` | Add `@Column(name = "status", columnDefinition = "interview_status")` |
| FIX | `notes` | Add `@Column(name = "notes")` |

### A12. Role Entity

| Action | Field | Details |
|--------|-------|---------|
| ADD | `dateCreated` | `@CreationTimestamp @Column(name = "date_created") OffsetDateTime dateCreated` |
| ADD | `permissions` | `@ManyToMany @JoinTable(name = "role_permissions", ...) Set<Permission> permissions` (after Permission entity is created in Phase B) |

### A13. JobApplication Entity

No structural changes needed. Minor annotation improvement:
- Add `columnDefinition = "application_status"` to status `@Column`

---

## Phase B: New Entities + Repositories

### B1. Permission Entity

```
Table: permissions
Fields: id (BIGINT PK), name (VARCHAR UNIQUE NOT NULL), date_created (TIMESTAMPTZ)
```

### B2. Moderator Entity

```
Table: moderators
Fields: id (BIGINT PK), account_id (FK accounts UNIQUE), full_name, phone, date_created, date_updated
Relationships: @OneToOne Account
```

### B3. CompanyVerification Entity

```
Table: company_verifications
Fields: id, company_id (FK), status (verification_status enum), note, submitted_by (FK recruiters), reviewed_by (FK moderators nullable), submitted_at, reviewed_at
Relationships: @ManyToOne Company, @ManyToOne Recruiter (submittedBy), @ManyToOne Moderator (reviewedBy)
```

### B4. CompanyVerificationAttachment Entity

```
Table: company_verification_attachments
Composite PK: (verification_id, resource_id)
Relationships: @ManyToOne CompanyVerification, @ManyToOne Resource
IdClass: CompanyVerificationAttachmentId
```

### B5. CandidateWorkExperience Entity

```
Table: candidate_work_experiences
Fields: id, candidate_id (FK), company_id (FK nullable), job_title, start_date (DATE), end_date (DATE nullable), is_current, description
Relationships: @ManyToOne Candidate, @ManyToOne Company (nullable)
```

### B6. School Entity

```
Table: schools
Fields: id (BIGINT PK), name (VARCHAR NOT NULL)
```

### B7. CandidateEducation Entity

```
Table: candidate_educations
Fields: id, candidate_id (FK), school_id (FK nullable), degree, field_of_study, start_year (INT), end_year (INT nullable), is_current, description
Relationships: @ManyToOne Candidate, @ManyToOne School
```

### B8. CandidateResume Entity

```
Table: candidate_resumes
Fields: id, candidate_id (FK), resource_id (FK), title, date_created
Relationships: @ManyToOne Candidate, @ManyToOne Resource
```

### B9. JobCategory Entity

```
Table: job_categories
Fields: id, name, slug (UNIQUE), parent_id (self FK nullable), path (LTREE mapped as String), is_leaf
Relationships: @ManyToOne JobCategory (parent), @OneToMany Set<JobCategory> (children)
Note: path column uses columnDefinition = "ltree", mapped as String
```

### B10. CandidatePosition (join entity)

```
Table: candidate_positions
Composite PK: (candidate_id, category_id)
Relationships: @ManyToOne Candidate, @ManyToOne JobCategory
IdClass: CandidatePositionId
```

### B11. Add Permission ManyToMany to Role

After Permission entity is created, add to Role:
```java
@ManyToMany
@JoinTable(name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"))
@Builder.Default
private Set<Permission> permissions = new HashSet<>();
```

### B12. Repositories

Create Spring Data JPA repositories for all new entities:
- `PermissionRepository`
- `ModeratorRepository`
- `CompanyVerificationRepository`
- `CompanyVerificationAttachmentRepository`
- `CandidateWorkExperienceRepository`
- `SchoolRepository`
- `CandidateEducationRepository`
- `CandidateResumeRepository`
- `JobCategoryRepository`
- `CandidatePositionRepository`

---

## Phase C: Remove Obsolete + Rewire Services

### C1. Delete Obsolete Entities

Files to delete:
- `domain/model/JobFamily.java`
- `domain/model/SubFamily.java`
- `domain/model/JobRole.java`
- `domain/model/AttestationResource.java`
- `domain/model/ids/AttestationResourceId.java`

### C2. Delete Obsolete Repositories

- `AttestationResourceRepository`
- `JobFamilyRepository` (if exists)
- `SubFamilyRepository` (if exists)
- `JobRoleRepository` (if exists)

### C3. Delete/Update Obsolete Mappers and DTOs

Find and remove/update all mappers and DTOs that reference:
- `JobFamily`, `SubFamily`, `JobRole`
- `AttestationResource`
- Old `ResourceType` values (AVATAR, CV, COMPANY_LOGO, JOB_ATTACHMENT, ATTESTATION)
- Removed fields: `Resource.url`, `Resource.mimeType`, `Resource.ownerId`, `Candidate.seniority`, `Candidate.salaryExpectMin/Max`, `CandidateSkill.level`

### C4. ResourceType Service Rewiring

All code using old ResourceType values must update:

| Old Value | New Value | Context |
|-----------|-----------|---------|
| `ResourceType.AVATAR` | `ResourceType.IMAGE` | Avatar uploads in ResourceServiceImpl, CandidateServiceImpl, RecruiterServiceImpl |
| `ResourceType.CV` | `ResourceType.DOCUMENT` | CV uploads in CandidateServiceImpl |
| `ResourceType.COMPANY_LOGO` | `ResourceType.IMAGE` | Logo uploads in ResourceServiceImpl |
| `ResourceType.JOB_ATTACHMENT` | `ResourceType.DOCUMENT` | Job attachment uploads |
| `ResourceType.ATTESTATION` | `ResourceType.DOCUMENT` | Attestation uploads in ResourceServiceImpl |

### C5. Resource Field Removal Ripple

Code that accesses `resource.getUrl()`, `resource.getMimeType()`, or `resource.getOwnerId()` must be updated:
- `CloudStorageService`/`CloudinaryStorageImpl` — these create Resource objects with url/mimeType
- `ResourceMapper` — maps Resource fields to DTOs
- Any service reading these fields

The `url` field removal means all code serving resource URLs to the frontend must reconstruct URLs from `public_id` using the Cloudinary base URL pattern. Add a utility method (e.g., on `CloudStorageService` or a helper) that builds the full URL from a public_id.

### C6. Candidate Field Rewiring

- `CandidateServiceImpl.updateProfileFromCV()` uses `candidate.setSeniority()` — must be removed or adapted
- `CandidateServiceImpl.calculateSeniority()` private method — remove entirely
- `CandidateMapper` — update field mappings for `salaryExpect` (single field)
- `CandidateRequest`/`CandidateResponse` DTOs — update field names

### C7. Company Attestation Flow Rewiring

The old flow:
- `ResourceServiceImpl.uploadAttestation()` creates `AttestationResource` entries
- `CompanyServiceImpl.getCompanyAttestations()` reads them

New flow must use `CompanyVerification` + `CompanyVerificationAttachment`. This involves:
- Creating a new company verification submission flow
- Updating the controller endpoints to match the new data model
- Admin/moderator verification review endpoints

### C8. Job Taxonomy Rewiring

- `JobServiceImpl` references to `JobRole` → `JobCategory`
- Job creation/update flows need `category_id` instead of `job_role_id`
- Job DTOs (CreateJobRequest, UpdateJobRequest, JobResponse) need field updates
- Job mappers need updates

### C9. OutboxEvent aggregateId Type Change

`OutboxEventService.saveOutboxEvent()` signature changes: `aggregateId` parameter from `Long` to `String`. All callers (JobServiceImpl, OutboxReconciliationService) must pass `String.valueOf(job.getId())` or similar.

---

## Phase D: Build Verification + Cleanup

- Run `mvn clean compile`
- Fix any remaining compilation errors
- Remove unused imports across all modified files
- Verify no references to deleted entities remain

---

## Risk Assessment

**Medium risk — Resource.url removal:** The `url` field is removed. Cloudinary URLs will be reconstructed from `public_id` at runtime (e.g., in a service method or mapper). All code that previously read `resource.getUrl()` must be updated to reconstruct the URL from `resource.getPublicId()` using the Cloudinary base URL pattern.

**Medium risk — Company verification flow rewrite:** The attestation → verification transition changes the business flow significantly. The new `CompanyVerification` entity introduces a multi-step workflow (submit → review) that didn't exist before.

**Low risk — Entity field fixes:** Table name, column name, and type fixes are mechanical and low-risk.

---

## Files Affected (Estimated)

- ~18 entity files (modify or create)
- ~10 repository files (create or delete)
- ~8 service impl files (rewire)
- ~6 mapper files (update)
- ~8 DTO files (update)
- ~4 controller files (update)
- ~4 enum files (create or modify)
- ~3 ID class files (create or delete)
- **Total: ~60 files**
