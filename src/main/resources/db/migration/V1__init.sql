-- =====================================================
-- EXTENSIONS
-- =====================================================

CREATE EXTENSION IF NOT EXISTS ltree;

-- =====================================================
-- ENUM TYPES
-- =====================================================

CREATE TYPE seniority_level AS ENUM (
  'INTERN', 'FRESHER', 'JUNIOR', 'MID', 'SENIOR', 'MANAGER'
);

CREATE TYPE experience_years AS ENUM (
  'NO_EXPERIENCE', 'UNDER_1_YEAR', 'ONE_YEAR', 'TWO_YEARS',
  'THREE_YEARS', 'FOUR_YEARS', 'FIVE_YEARS', 'OVER_5_YEARS'
);

CREATE TYPE employment_type AS ENUM (
  'FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'VOLUNTEER', 'TEMPORARY'
);

CREATE TYPE account_status AS ENUM ('ACTIVE', 'SUSPENDED');

CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE');

CREATE TYPE work_mode AS ENUM ('ONSITE', 'REMOTE', 'HYBRID');

CREATE TYPE job_status AS ENUM ('DRAFT', 'PENDING', 'PUBLISHED', 'EXPIRED', 'CANCELED');

CREATE TYPE application_status AS ENUM (
  'SUBMITTED', 'REVIEWED', 'INTERVIEW', 'OFFERED', 'REJECTED'
);

CREATE TYPE verification_status AS ENUM ('SUBMITTED', 'APPROVED', 'REJECTED');

CREATE TYPE resource_type AS ENUM ('IMAGE', 'DOCUMENT', 'VIDEO');

CREATE TYPE interaction_event_type AS ENUM (
  'APPLY', 'SAVE', 'CLICK',
  'CLICK_FROM_SEARCH', 'CLICK_FROM_SIMILAR', 'CLICK_FROM_RECOMMENDED',
  'SKIP_FROM_SEARCH', 'SKIP_FROM_SIMILAR', 'SKIP_FROM_RECOMMENDED'
);

CREATE TYPE interview_status AS ENUM ('SCHEDULED', 'COMPLETED', 'CANCELED', 'NO_SHOW');

CREATE TYPE outbox_status AS ENUM ('PENDING', 'SENT', 'FAILED', 'DLQ');

CREATE TYPE company_size AS ENUM ('MICRO', 'SMALL', 'MEDIUM', 'LARGE', 'ENTERPRISE');

-- =====================================================
-- CORE — RBAC & ACCOUNTS
-- =====================================================

CREATE TABLE roles (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name       VARCHAR(100) UNIQUE NOT NULL,
  date_created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE permissions (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name       VARCHAR(100) UNIQUE NOT NULL,
  date_created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE role_permissions (
  role_id       BIGINT NOT NULL REFERENCES roles(id),
  permission_id BIGINT NOT NULL REFERENCES permissions(id),
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE accounts (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email        VARCHAR(255) UNIQUE NOT NULL,
  password     VARCHAR(255),
  role_id      BIGINT NOT NULL REFERENCES roles(id),
  status       account_status NOT NULL DEFAULT 'ACTIVE',
  provider     auth_provider NOT NULL,
  verified_at  TIMESTAMPTZ,
  date_created TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_accounts_email ON accounts(email);
CREATE INDEX idx_accounts_status_provider ON accounts(status, provider);

-- =====================================================
-- LOCATIONS
-- =====================================================

CREATE TABLE locations (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  street_address VARCHAR(255),
  ward           VARCHAR(120),
  district       VARCHAR(120),
  province_city  VARCHAR(120),
  country        VARCHAR(120),
  lat            DECIMAL(10, 7),
  lng            DECIMAL(10, 7),
  date_created   TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated   TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT chk_locations_lat CHECK (lat BETWEEN -90 AND 90),
  CONSTRAINT chk_locations_lng CHECK (lng BETWEEN -180 AND 180)
);

-- =====================================================
-- RESOURCES
-- =====================================================

CREATE TABLE resources (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  content_type  TEXT NOT NULL,
  resource_type resource_type NOT NULL,
  public_id     VARCHAR(255) UNIQUE NOT NULL,
  size          BIGINT NOT NULL,
  name          TEXT NOT NULL,
  uploaded_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_resources_public_id ON resources(public_id);

-- =====================================================
-- COMPANIES
-- =====================================================

CREATE TABLE companies (
  id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name             VARCHAR(200) NOT NULL,
  website          VARCHAR(255),
  size             company_size,
  description      TEXT,
  phone            VARCHAR(20),
  email            VARCHAR(255),
  industry         VARCHAR(100),
  logo_resource_id BIGINT REFERENCES resources(id),
  is_verified      BOOLEAN NOT NULL DEFAULT false,
  date_created     TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_companies_name ON companies(name);
CREATE INDEX idx_companies_industry ON companies(industry);

CREATE TABLE company_locations (
  company_id    BIGINT NOT NULL REFERENCES companies(id),
  location_id   BIGINT NOT NULL REFERENCES locations(id),
  is_headquarter BOOLEAN NOT NULL DEFAULT false,
  PRIMARY KEY (company_id, location_id)
);

-- =====================================================
-- USER PROFILES
-- =====================================================

CREATE TABLE moderators (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  account_id   BIGINT UNIQUE NOT NULL REFERENCES accounts(id),
  full_name    VARCHAR(150),
  phone        VARCHAR(20),
  date_created TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE candidates (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  account_id        BIGINT UNIQUE NOT NULL REFERENCES accounts(id),
  full_name         VARCHAR(150),
  phone             VARCHAR(20),
  location_id       BIGINT REFERENCES locations(id),
  experience_years  experience_years,
  salary_expect     INT,
  currency          VARCHAR(10),
  remote_pref       BOOLEAN,
  relocation_pref   BOOLEAN,
  avatar_resource_id BIGINT REFERENCES resources(id),
  bio               TEXT,
  date_created      TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_candidates_account_id ON candidates(account_id);
CREATE INDEX idx_candidates_location_id ON candidates(location_id);
CREATE INDEX idx_candidates_experience_years ON candidates(experience_years);

CREATE TABLE recruiters (
  id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  account_id        BIGINT UNIQUE NOT NULL REFERENCES accounts(id),
  full_name         VARCHAR(150),
  phone             VARCHAR(20),
  avatar_resource_id BIGINT REFERENCES resources(id),
  company_id        BIGINT NOT NULL REFERENCES companies(id),
  date_created      TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_recruiters_account_id ON recruiters(account_id);
CREATE INDEX idx_recruiters_company_id ON recruiters(company_id);

-- =====================================================
-- COMPANY VERIFICATIONS
-- =====================================================

CREATE TABLE company_verifications (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  company_id    BIGINT NOT NULL REFERENCES companies(id),
  status        verification_status NOT NULL DEFAULT 'SUBMITTED',
  note          TEXT,
  submitted_by  BIGINT NOT NULL REFERENCES recruiters(id),
  reviewed_by   BIGINT REFERENCES moderators(id),
  submitted_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  reviewed_at   TIMESTAMPTZ
);

CREATE INDEX idx_company_verifications_company_id ON company_verifications(company_id);
CREATE INDEX idx_company_verifications_status ON company_verifications(status);

CREATE TABLE company_verification_attachments (
  verification_id BIGINT NOT NULL REFERENCES company_verifications(id),
  resource_id     BIGINT NOT NULL REFERENCES resources(id),
  PRIMARY KEY (verification_id, resource_id)
);

-- =====================================================
-- CANDIDATE DETAILS
-- =====================================================

CREATE TABLE candidate_work_experiences (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  company_id   BIGINT REFERENCES companies(id),
  job_title    VARCHAR(150),
  start_date   DATE,
  end_date     DATE,
  is_current   BOOLEAN NOT NULL DEFAULT false,
  description  TEXT,

  CONSTRAINT chk_work_exp_dates CHECK (end_date IS NULL OR end_date >= start_date)
);

CREATE INDEX idx_candidate_work_exp_candidate_id ON candidate_work_experiences(candidate_id);

CREATE TABLE schools (
  id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR(255) NOT NULL
);

CREATE TABLE candidate_educations (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  candidate_id   BIGINT NOT NULL REFERENCES candidates(id),
  school_id      BIGINT REFERENCES schools(id),
  degree         VARCHAR(150),
  field_of_study VARCHAR(150),
  start_year     INT,
  end_year       INT,
  is_current     BOOLEAN NOT NULL DEFAULT false,
  description    TEXT,

  CONSTRAINT chk_education_years CHECK (end_year IS NULL OR end_year >= start_year)
);

CREATE INDEX idx_candidate_educations_candidate_id ON candidate_educations(candidate_id);

CREATE TABLE candidate_resumes (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  resource_id  BIGINT NOT NULL REFERENCES resources(id),
  title        VARCHAR(100),
  date_created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_candidate_resumes_candidate_id ON candidate_resumes(candidate_id);

-- =====================================================
-- JOB TAXONOMY
-- =====================================================

CREATE TABLE job_categories (
  id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name      VARCHAR(100) NOT NULL,
  slug      VARCHAR(100) UNIQUE NOT NULL,
  parent_id BIGINT REFERENCES job_categories(id),
  path      LTREE,
  is_leaf   BOOLEAN NOT NULL DEFAULT false
);

CREATE INDEX idx_job_categories_parent_id ON job_categories(parent_id);
CREATE INDEX idx_job_categories_slug ON job_categories(slug);
CREATE INDEX idx_job_categories_path ON job_categories USING GIST(path);

CREATE TABLE candidate_positions (
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  category_id  BIGINT NOT NULL REFERENCES job_categories(id),
  PRIMARY KEY (candidate_id, category_id)
);

-- =====================================================
-- SKILLS
-- =====================================================

CREATE TABLE skills (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name         VARCHAR(100) UNIQUE NOT NULL,
  aliases      JSONB,
  date_created TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_skills_name ON skills(name);

CREATE TABLE candidate_skills (
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  skill_id     BIGINT NOT NULL REFERENCES skills(id),
  PRIMARY KEY (candidate_id, skill_id)
);

-- =====================================================
-- JOBS
-- =====================================================

CREATE TABLE jobs (
  id                   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  company_id           BIGINT NOT NULL REFERENCES companies(id),
  recruiter_id         BIGINT NOT NULL REFERENCES recruiters(id),
  title                VARCHAR(200) NOT NULL,
  category_id          BIGINT REFERENCES job_categories(id),
  seniority            seniority_level NOT NULL,
  employment_type      employment_type NOT NULL,
  min_experience_years INT,
  location_id          BIGINT REFERENCES locations(id),
  work_mode            work_mode NOT NULL,
  salary_min           INT,
  salary_max           INT,
  currency             VARCHAR(10),
  max_candidates       INT,
  date_posted          TIMESTAMPTZ,
  date_expires         TIMESTAMPTZ,
  status               job_status NOT NULL DEFAULT 'DRAFT',
  date_created         TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated         TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT chk_jobs_salary CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max),
  CONSTRAINT chk_jobs_max_candidates CHECK (max_candidates IS NULL OR max_candidates > 0),
  CONSTRAINT chk_jobs_dates CHECK (date_expires IS NULL OR date_posted IS NULL OR date_expires > date_posted)
);

CREATE INDEX idx_jobs_company_id ON jobs(company_id);
CREATE INDEX idx_jobs_recruiter_id ON jobs(recruiter_id);
CREATE INDEX idx_jobs_category_id ON jobs(category_id);
CREATE INDEX idx_jobs_location_id ON jobs(location_id);
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_status_date_posted ON jobs(status, date_posted);
CREATE INDEX idx_jobs_filters ON jobs(seniority, employment_type, work_mode);

CREATE TABLE job_descriptions (
  job_id           BIGINT PRIMARY KEY REFERENCES jobs(id),
  summary          TEXT,
  responsibilities TEXT,
  requirements     TEXT,
  nice_to_have     TEXT,
  benefits         TEXT,
  hiring_process   TEXT,
  notes            TEXT
);

CREATE TABLE job_skill_requirements (
  job_id   BIGINT NOT NULL REFERENCES jobs(id),
  skill_id BIGINT NOT NULL REFERENCES skills(id),
  PRIMARY KEY (job_id, skill_id)
);

-- =====================================================
-- APPLICATIONS
-- =====================================================

CREATE TABLE job_applications (
  id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  candidate_id    BIGINT NOT NULL REFERENCES candidates(id),
  job_id          BIGINT NOT NULL REFERENCES jobs(id),
  status          application_status NOT NULL DEFAULT 'SUBMITTED',
  cv_resource_id  BIGINT NOT NULL REFERENCES resources(id),
  applied_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT uq_job_applications_candidate_job UNIQUE (candidate_id, job_id)
);

CREATE INDEX idx_job_applications_candidate_id ON job_applications(candidate_id);
CREATE INDEX idx_job_applications_job_id ON job_applications(job_id);
CREATE INDEX idx_job_applications_status ON job_applications(status);

CREATE TABLE interviews (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  application_id BIGINT NOT NULL REFERENCES job_applications(id),
  scheduled_at   TIMESTAMPTZ NOT NULL,
  location_id    BIGINT REFERENCES locations(id),
  meeting_url    VARCHAR(500),
  status         interview_status NOT NULL DEFAULT 'SCHEDULED',
  notes          TEXT,
  date_created   TIMESTAMPTZ NOT NULL DEFAULT now(),
  date_updated   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_interviews_application_id ON interviews(application_id);
CREATE INDEX idx_interviews_status ON interviews(status);
CREATE INDEX idx_interviews_scheduled_at ON interviews(scheduled_at);

CREATE TABLE saved_jobs (
  id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  job_id       BIGINT NOT NULL REFERENCES jobs(id),
  saved_at     TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT uq_saved_jobs_candidate_job UNIQUE (candidate_id, job_id)
);

CREATE INDEX idx_saved_jobs_candidate_id ON saved_jobs(candidate_id);
CREATE INDEX idx_saved_jobs_job_id ON saved_jobs(job_id);

-- =====================================================
-- ANALYTICS
-- =====================================================

CREATE TABLE user_interactions (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  account_id  BIGINT REFERENCES accounts(id),
  event_type  interaction_event_type NOT NULL,
  external_id VARCHAR(64) UNIQUE,
  job_id      BIGINT REFERENCES jobs(id),
  metadata    JSONB,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_user_interactions_account_event ON user_interactions(account_id, event_type);
CREATE INDEX idx_user_interactions_job_id ON user_interactions(job_id);
CREATE INDEX idx_user_interactions_occurred_at ON user_interactions(occurred_at);

CREATE TABLE outbox_events (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  aggregate_type VARCHAR(50) NOT NULL,
  aggregate_id   VARCHAR(64) NOT NULL,
  event_type     VARCHAR(50) NOT NULL,
  payload        JSONB NOT NULL,
  occurred_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  status         outbox_status NOT NULL DEFAULT 'PENDING',
  attempts       INT NOT NULL DEFAULT 0,
  processed_at   TIMESTAMPTZ,
  trace_id       UUID
);

CREATE INDEX idx_outbox_events_status_occurred ON outbox_events(status, occurred_at);
CREATE INDEX idx_outbox_events_aggregate ON outbox_events(aggregate_type, aggregate_id);
CREATE INDEX idx_outbox_events_trace_id ON outbox_events(trace_id);
