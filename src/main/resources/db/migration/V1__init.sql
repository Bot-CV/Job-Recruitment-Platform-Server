-- =====================================================
-- Database Schema Script
-- Job Recruitment Platform
-- =====================================================
-- =====================================================
-- ENUMS
-- =====================================================
CREATE TYPE seniority_level AS ENUM (
    'INTERN',
    'FRESHER',
    'JUNIOR',
    'MID',
    'SENIOR',
    'MANAGER'
);

CREATE TYPE employment_type AS ENUM (
    'FULL_TIME',
    'PART_TIME',
    'CONTRACT',
    'INTERNSHIP',
    'VOLUNTEER',
    'TEMPORARY'
);

CREATE TYPE account_status AS ENUM ('ACTIVE', 'SUSPENDED');

CREATE TYPE auth_provider AS ENUM ('LOCAL', 'GOOGLE');

CREATE TYPE work_mode AS ENUM ('ONSITE', 'REMOTE', 'HYBRID');

CREATE TYPE job_status AS ENUM (
    'DRAFT',
    'PENDING',
    'PUBLISHED',
    'EXPIRED',
    'CANCELED'
);

CREATE TYPE application_status AS ENUM (
    'SUBMITTED',
    'REVIEWED',
    'INTERVIEW',
    'OFFERED',
    'REJECTED'
);

CREATE TYPE resource_type AS ENUM ('AVATAR', 'CV');

-- =====================================================
-- CORE TABLES
-- =====================================================
CREATE TABLE
    roles (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE
    );

-- permissions removed
CREATE TABLE
    accounts (
        id BIGSERIAL PRIMARY KEY,
        email VARCHAR(255) NOT NULL UNIQUE,
        password VARCHAR(255),
        role_id BIGINT NOT NULL,
        status account_status NOT NULL DEFAULT 'ACTIVE',
        provider auth_provider NOT NULL,
        verified_at TIMESTAMPTZ (3),
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_accounts_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE RESTRICT
    );

CREATE INDEX idx_accounts_email ON accounts (email);

CREATE INDEX idx_accounts_role ON accounts (role_id);

CREATE INDEX idx_accounts_status ON accounts (status);

-- =====================================================
-- LOCATION TABLES
-- =====================================================
CREATE TABLE
    locations (
        id BIGSERIAL PRIMARY KEY,
        street_address VARCHAR(255),
        ward VARCHAR(120),
        district VARCHAR(120),
        province_city VARCHAR(120),
        country VARCHAR(120),
        lat DECIMAL(10, 7),
        lng DECIMAL(10, 7)
    );

CREATE INDEX idx_locations_province_city ON locations (province_city);

CREATE INDEX idx_locations_district ON locations (district);

CREATE INDEX idx_locations_country ON locations (country);

-- =====================================================
-- COMPANY TABLES
-- =====================================================
CREATE TABLE
    companies (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(200) NOT NULL,
        website VARCHAR(255),
        size VARCHAR(50), -- Số lượng nhân viên thể hiện quy mô công ty
        logo_resource_id BIGINT,
        verified BOOLEAN NOT NULL DEFAULT FALSE,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW ()
    );

CREATE INDEX idx_companies_name ON companies (name);

CREATE INDEX idx_companies_verified ON companies (verified);

CREATE TABLE
    company_location (
        company_id BIGINT NOT NULL,
        location_id BIGINT NOT NULL,
        is_headquarter BOOLEAN DEFAULT FALSE,
        PRIMARY KEY (company_id, location_id),
        CONSTRAINT fk_company_location_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
        CONSTRAINT fk_company_location_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE CASCADE
    );

CREATE INDEX idx_company_location_company ON company_location (company_id);

CREATE INDEX idx_company_location_location ON company_location (location_id);

-- =====================================================
-- USER PROFILE TABLES
-- =====================================================
CREATE TABLE
    candidates (
        id BIGSERIAL PRIMARY KEY,
        account_id BIGINT NOT NULL UNIQUE,
        full_name VARCHAR(150),
        location_id BIGINT,
        seniority seniority_level,
        salary_expect_min INTEGER,
        salary_expect_max INTEGER,
        currency VARCHAR(10),
        remote_pref BOOLEAN,
        relocation_pref BOOLEAN,
        avatar_resource_id BIGINT NOT NULL,
        bio TEXT,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_candidates_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
        CONSTRAINT fk_candidates_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL
    );

CREATE INDEX idx_candidates_account ON candidates (account_id);

CREATE INDEX idx_candidates_location ON candidates (location_id);

CREATE INDEX idx_candidates_seniority ON candidates (seniority);

CREATE TABLE
    recruiters (
        id BIGSERIAL PRIMARY KEY,
        account_id BIGINT NOT NULL UNIQUE,
        full_name VARCHAR(150),
        avatar_resource_id BIGINT NOT NULL,
        company_id BIGINT NOT NULL UNIQUE,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_recruiters_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE,
        CONSTRAINT fk_recruiters_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE
    );

CREATE INDEX idx_recruiters_account ON recruiters (account_id);

CREATE INDEX idx_recruiters_company ON recruiters (company_id);

-- =====================================================
-- JOB TAXONOMY TABLES
-- =====================================================
CREATE TABLE
    job_families (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE, -- Công nghệ thông tin, Thiết kế, Logistics/Thu mua/Kho/Vận tải, ...
        slug VARCHAR(100) NOT NULL UNIQUE,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW ()
    );

CREATE INDEX idx_job_families_slug ON job_families (slug);

CREATE TABLE
    sub_families (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE, -- Software Engineering, Software Testing, AI, ....
        job_family_id BIGINT NOT NULL,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_sub_families_job_family FOREIGN KEY (job_family_id) REFERENCES job_families (id) ON DELETE CASCADE
    );

CREATE INDEX idx_sub_families_job_family ON sub_families (job_family_id);

CREATE TABLE
    job_roles (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE, -- Software Engineer, Backend Developer, Frontend Developer, Mobile Developer, Fullstack Developer, Blockchain Engineer, ...
        sub_family_id BIGINT NOT NULL,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        date_updated TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_job_roles_sub_family FOREIGN KEY (sub_family_id) REFERENCES sub_families (id) ON DELETE CASCADE
    );

CREATE INDEX idx_job_roles_sub_family ON job_roles (sub_family_id);

-- =====================================================
-- SKILLS TABLES
-- =====================================================
CREATE TABLE
    skills (
        id BIGSERIAL PRIMARY KEY,
        name VARCHAR(100) NOT NULL UNIQUE,
        aliases JSONB,
        date_created TIMESTAMPTZ (3) NOT NULL DEFAULT NOW ()
    );

CREATE INDEX idx_skills_name ON skills (name);

CREATE INDEX idx_skills_aliases ON skills USING GIN (aliases);

CREATE TABLE
    candidate_skills (
        candidate_id BIGINT NOT NULL,
        skill_id BIGINT NOT NULL,
        level INTEGER CHECK (
            level >= 0
            AND level <= 5
        ), -- 0..5
        PRIMARY KEY (candidate_id, skill_id),
        CONSTRAINT fk_candidate_skills_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE,
        CONSTRAINT fk_candidate_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
    );

CREATE INDEX idx_candidate_skills_candidate ON candidate_skills (candidate_id);

CREATE INDEX idx_candidate_skills_skill ON candidate_skills (skill_id);

CREATE INDEX idx_candidate_skills_level ON candidate_skills (level);

-- =====================================================
-- JOB TABLES
-- =====================================================
CREATE TABLE
    jobs (
        id BIGSERIAL PRIMARY KEY,
        company_id BIGINT NOT NULL,
        title VARCHAR(200) NOT NULL,
        job_role_id BIGINT,
        seniority seniority_level NOT NULL,
        employment_type employment_type NOT NULL,
        min_experience_years INTEGER,
        location_id BIGINT,
        work_mode work_mode NOT NULL,
        salary_min INTEGER,
        salary_max INTEGER,
        currency VARCHAR(10),
        date_posted TIMESTAMPTZ (3),
        date_expires TIMESTAMPTZ (3),
        status job_status NOT NULL,
        CONSTRAINT fk_jobs_company FOREIGN KEY (company_id) REFERENCES companies (id) ON DELETE CASCADE,
        CONSTRAINT fk_jobs_job_role FOREIGN KEY (job_role_id) REFERENCES job_roles (id) ON DELETE SET NULL,
        CONSTRAINT fk_jobs_location FOREIGN KEY (location_id) REFERENCES locations (id) ON DELETE SET NULL
    );

CREATE INDEX idx_jobs_company ON jobs (company_id);

CREATE INDEX idx_jobs_job_role ON jobs (job_role_id);

CREATE INDEX idx_jobs_location ON jobs (location_id);

CREATE INDEX idx_jobs_status ON jobs (status);

CREATE INDEX idx_jobs_seniority ON jobs (seniority);

CREATE INDEX idx_jobs_work_mode ON jobs (work_mode);

CREATE INDEX idx_jobs_date_posted ON jobs (date_posted DESC);

CREATE INDEX idx_jobs_date_expires ON jobs (date_expires);

-- Job Descriptions
CREATE TABLE
    job_description (
        id BIGSERIAL PRIMARY KEY,
        job_id BIGINT NOT NULL,
        summary TEXT,
        responsibilities TEXT,
        requirements TEXT,
        nice_to_have TEXT,
        benefits TEXT,
        hiring_process TEXT,
        notes TEXT,
        CONSTRAINT fk_job_description_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
        CONSTRAINT uq_job_description_job UNIQUE (job_id)
    );

CREATE INDEX idx_job_description_job ON job_description (job_id);

CREATE TABLE
    job_skill_requirements (
        job_id BIGINT NOT NULL,
        skill_id BIGINT NOT NULL,
        --         level INTEGER NOT NULL CHECK (
        --             level >= 0
        --             AND level <= 5
        --         ),
        PRIMARY KEY (job_id, skill_id),
        CONSTRAINT fk_job_skill_requirements_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
        CONSTRAINT fk_job_skill_requirements_skill FOREIGN KEY (skill_id) REFERENCES skills (id) ON DELETE CASCADE
    );

CREATE INDEX idx_job_skill_requirements_job ON job_skill_requirements (job_id);

CREATE INDEX idx_job_skill_requirements_skill ON job_skill_requirements (skill_id);

-- =====================================================
-- APPLICATION TABLES
-- =====================================================
CREATE TABLE
    job_applications (
        id BIGSERIAL PRIMARY KEY,
        candidate_id BIGINT NOT NULL,
        job_id BIGINT NOT NULL,
        status application_status NOT NULL DEFAULT 'SUBMITTED',
        cv_resource_id BIGINT NOT NULL,
        applied_at TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_job_applications_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE,
        CONSTRAINT fk_job_applications_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
        CONSTRAINT uq_job_applications_candidate_job UNIQUE (candidate_id, job_id)
    );

CREATE INDEX idx_job_applications_candidate ON job_applications (candidate_id);

CREATE INDEX idx_job_applications_job ON job_applications (job_id);

CREATE INDEX idx_job_applications_status ON job_applications (status);

CREATE INDEX idx_job_applications_applied_at ON job_applications (applied_at DESC);

CREATE TABLE
    saved_jobs (
        id BIGSERIAL PRIMARY KEY,
        candidate_id BIGINT NOT NULL,
        job_id BIGINT NOT NULL,
        saved_at TIMESTAMPTZ (3) NOT NULL DEFAULT NOW (),
        CONSTRAINT fk_saved_jobs_candidate FOREIGN KEY (candidate_id) REFERENCES candidates (id) ON DELETE CASCADE,
        CONSTRAINT fk_saved_jobs_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
        CONSTRAINT uq_saved_jobs_candidate_job UNIQUE (candidate_id, job_id)
    );

CREATE INDEX idx_saved_jobs_candidate ON saved_jobs (candidate_id);

CREATE INDEX idx_saved_jobs_job ON saved_jobs (job_id);

CREATE INDEX idx_saved_jobs_saved_at ON saved_jobs (saved_at DESC);

-- =====================================================
-- RESOURCE TABLES
-- =====================================================
CREATE TABLE
    resources (
        id BIGSERIAL PRIMARY KEY,
        mime_type TEXT NOT NULL,
        resource_type resource_type NOT NULL,
        url TEXT NOT NULL,
        public_id VARCHAR(36) NOT NULL UNIQUE,
        name TEXT NOT NULL,
        uploaded_at TIMESTAMPTZ (3) NOT NULL DEFAULT NOW ()
    );

CREATE INDEX idx_resources_uploaded_at ON resources (uploaded_at DESC);