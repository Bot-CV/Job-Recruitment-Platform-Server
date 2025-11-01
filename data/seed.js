import 'dotenv/config';
import pkg from 'pg';
import { faker } from '@faker-js/faker/locale/vi';

/**
 * Seed & Utilities for Job Recruitment Platform
 * - Matches enums & tables in V1__init.sql
 * - Seeds ALL columns for each table where applicable (even optional ones like companies.email/industry)
 * - Adds pools for AVATAR, CV, COMPANY_LOGO, JOB_ATTACHMENT
 * - Creates interviews for a subset of job_applications
 *
 * Notes:
 * - Explicit enum/jsonb casts to avoid 'could not determine data type of parameter $X'
 * - Idempotent-ish via ON CONFLICT where possible
 */

const { Pool } = pkg;
const pool = new Pool(
    process.env.DATABASE_URL ? { connectionString: process.env.DATABASE_URL } : {}
);

/* ---------- helpers ---------- */
const cap = (s) => (s && s.length ? s.charAt(0).toUpperCase() + s.slice(1) : s);
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randInt = (min, max) => Math.floor(Math.random() * (max - min + 1)) + min;
const recent = (days) => faker.date.recent({ days });
const soon = (days, refDate) => faker.date.soon({ days, refDate });
const shuffle = (arr) => {
    const a = arr.slice();
    for (let i = a.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
};

/* ---------- enums (MUST MATCH DB ENUMS) ---------- */
const ENUM = {
    seniority_level: ['INTERN','FRESHER','JUNIOR','MID','SENIOR','MANAGER'],
    employment_type: ['FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','VOLUNTEER','TEMPORARY'],
    account_status: ['ACTIVE','SUSPENDED'],
    auth_provider: ['LOCAL','GOOGLE'],
    work_mode: ['ONSITE','REMOTE','HYBRID'],
    job_status: ['DRAFT','PENDING','PUBLISHED','EXPIRED','CANCELED'],
    application_status: ['SUBMITTED','REVIEWED','INTERVIEW','OFFERED','REJECTED'],
    resource_type: ['AVATAR','CV','COMPANY_LOGO','JOB_ATTACHMENT'],
    analytics_event_type: ['SEARCH_QUERY','JOB_VIEWED','JOB_APPLIED','JOB_SAVED'],
    interview_status: ['SCHEDULED','COMPLETED','CANCELED','NO_SHOW'],
};

/* ---------- sizes ---------- */
const N = {
    roles: 3,
    accounts: 80,

    locations: 60,

    companies: 20,
    recruiters: 15, // ≤ companies (unique company_id per recruiter)

    candidates: 50,

    jobFamilies: 6,
    subFamilies: 12,
    jobRoles: 30,

    skills: 40,

    jobs: 80,

    jobApps: 120,
    savedJobs: 120,

    avatars: 160,         // for candidates/recruiters
    cvs: 220,             // for job applications
    companyLogos: 40,     // COMPANY_LOGO
    jobAttachments: 120,  // JOB_ATTACHMENT for jobs

    analytics: 220, // random activity events
    interviews: 80, // upper cap; actual created depends on applications
};

/* ---------- VN geo helpers (đơn giản) ---------- */
const VN_PROVINCES = [
    'Hồ Chí Minh','Hà Nội','Đà Nẵng','Bình Dương','Đồng Nai','Khánh Hòa',
    'Cần Thơ','Hải Phòng','Thừa Thiên Huế','Quảng Ninh','Bắc Ninh',
];
const INDUSTRIES = [
    'Information Technology','Finance','E-commerce','Logistics','Manufacturing','Healthcare','Education','Media','Gaming','Real Estate'
];

const randomWard = () => Math.random() < 0.5 ? `Phường ${cap(faker.word.sample())}` : `Xã ${cap(faker.word.sample())}`;
const randomDistrict = () => Math.random() < 0.6 ? `Quận ${randInt(1, 12)}` : `Huyện ${cap(faker.word.sample())}`;
const randomStreetAddress = () => {
    const streetName = faker.location.street() || `Đường ${cap(faker.word.sample())}`;
    return `${randInt(1, 299)} ${streetName}`;
};

/* ---------- generic bulk insert helper ---------- */
/**
 * insertMany(client, 'table_name', ['col1','col2'], rows, casts)
 * - rows: array of arrays [[v1,v2], [v1,v2], ...]
 * - casts: optional array of SQL casts for each column (e.g., ['::text','::event_type','::jsonb'])
 */
async function insertMany(client, table, columns, rows, casts = []) {
    if (!rows.length) return;
    const colList = columns.map(c => `"${c}"`).join(', ');
    let placeholders = [];
    const values = [];
    let idx = 1;
    for (const row of rows) {
        const parts = [];
        for (let j = 0; j < row.length; j++) {
            const cast = casts[j] || '';
            parts.push(`$${idx++}${cast}`);
            // JSON: ensure objects go as JSON strings so pg can coerce properly
            const v = (typeof row[j] === 'object' && row[j] !== null && !(row[j] instanceof Date))
                ? JSON.stringify(row[j])
                : row[j];
            values.push(v);
        }
        placeholders.push(`(${parts.join(', ')})`);
    }
    const sql = `INSERT INTO ${table} (${colList}) VALUES ${placeholders.join(', ')}`;
    await client.query(sql, values);
}

/* ---------- resources helpers ---------- */
async function insertResource(client, { type, ownerId = null }) {
    // ví dụ: 'image/png', 'application/pdf'
    const mimeType = pick(['image/png', 'image/jpeg', 'application/pdf']);
    // content_type = phần trước dấu '/' của mime_type (image | application | ...)
    const contentType = mimeType.split('/')[0];

    const { rows } = await client.query(
        `INSERT INTO resources(
       owner_id,
       mime_type,
       content_type,          -- NEW column
       resource_type,
       url,
       public_id,
       name
     )
     VALUES ($1, $2, $3, $4::resource_type, $5, $6, $7)
     RETURNING id`,
        [
            ownerId,
            mimeType,
            contentType,           // e.g. 'image' | 'application'
            type,                  // 'AVATAR' | 'CV' | 'COMPANY_LOGO' | 'JOB_ATTACHMENT'
            faker.internet.url(),
            faker.string.uuid(),
            faker.system.fileName(),
        ]
    );
    return rows[0].id;
}

/* ---------- BULK UPDATE analytics helper ---------- */
async function updateAnalyticsBulk(client, updates) {
    if (!updates?.length) return { updated: 0 };
    const rows = [];
    for (const u of updates) {
        rows.push([
            u.id,
            u.event_type ?? null,
            u.target_id ?? null,
            u.metadata ?? null,
            u.occurred_at ?? null,
        ]);
    }

    const values = [];
    let i = 1;
    const tuples = rows.map(r => {
        const tuple = `($${i++}::bigint, $${i++}::event_type, $${i++}::bigint, $${i++}::jsonb, $${i++}::timestamptz)`;
        values.push(
            r[0],
            r[1],
            r[2],
            r[3] ? JSON.stringify(r[3]) : null,
            r[4]
        );
        return tuple;
    }).join(', ');

    const sql = `
    WITH data(id, event_type, target_id, metadata, occurred_at) AS (
      VALUES ${tuples}
    )
    UPDATE analytics a
    SET
      event_type = COALESCE(d.event_type, a.event_type),
      target_id   = COALESCE(d.target_id,   a.target_id),
      metadata    = COALESCE(d.metadata,    a.metadata),
      occurred_at = COALESCE(d.occurred_at, a.occurred_at)
    FROM data d
    WHERE a.id = d.id
    RETURNING a.id;
  `;

    const res = await client.query(sql, values);
    return { updated: res.rowCount };
}

async function main() {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        /* =========================================
         * roles
         * ========================================= */
        const roleNames = ['ADMIN', 'RECRUITER', 'CANDIDATE'].slice(0, N.roles);
        const roleIds = [];
        const roleMap = {};
        for (const name of roleNames) {
            const { rows } = await client.query(
                `INSERT INTO roles(name)
                 VALUES ($1)
                     ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
                                               RETURNING id`,
                [name]
            );
            const id = rows[0].id;
            roleIds.push(id);
            roleMap[name] = id;
        }

        /* =========================================
         * accounts
         * ========================================= */
        const accountIds = [];
        const emails = new Set();
        for (let i = 0; i < N.accounts; i++) {
            let email;
            do {
                email = faker.internet.email().toLowerCase();
            } while (emails.has(email));
            emails.add(email);

            const roleId = pick(roleIds);
            const provider = pick(ENUM.auth_provider);
            const verifiedAt = Math.random() < 0.7 ? recent(120) : null;

            const { rows } = await client.query(
                `INSERT INTO accounts(
            email, password, role_id, status, provider, verified_at
         )
         VALUES ($1, $2, $3, $4::account_status, $5::auth_provider, $6)
         ON CONFLICT (email) DO NOTHING
         RETURNING id`,
                [
                    email,
                    'password123', // demo only, hash in real life
                    roleId,
                    pick(ENUM.account_status),
                    provider,
                    verifiedAt,
                ]
            );

            if (rows[0]) accountIds.push(rows[0].id);
        }

        /* =========================================
         * locations
         * ========================================= */
        const locationIds = [];
        for (let i = 0; i < N.locations; i++) {
            const province = pick(VN_PROVINCES);
            const district = randomDistrict();
            const ward = randomWard();
            const street = randomStreetAddress();
            const country = 'Việt Nam';
            const lat = faker.location.latitude({ min: 8.2, max: 23.4, precision: 7 });
            const lng = faker.location.longitude({ min: 102.1, max: 109.5, precision: 7 });

            const { rows } = await client.query(
                `INSERT INTO locations(
            street_address, ward, district, province_city, country, lat, lng
         )
         VALUES ($1, $2, $3, $4, $5, $6, $7)
         RETURNING id`,
                [street, ward, district, province, country, lat, lng]
            );
            locationIds.push(rows[0].id);
        }

        /* =========================================
         * resources pools
         * ========================================= */
        const avatarResourceIds = [];
        for (let i = 0; i < N.avatars; i++) {
            // owner left null; will be attached logically by foreign tables
            avatarResourceIds.push(await insertResource(client, { type: 'AVATAR', ownerId: null }));
        }

        const cvResourceIds = [];
        for (let i = 0; i < N.cvs; i++) {
            cvResourceIds.push(await insertResource(client, { type: 'CV', ownerId: null }));
        }

        const companyLogoResourceIds = [];
        for (let i = 0; i < N.companyLogos; i++) {
            companyLogoResourceIds.push(await insertResource(client, { type: 'COMPANY_LOGO', ownerId: null }));
        }

        const jobAttachmentResourceIds = [];
        for (let i = 0; i < N.jobAttachments; i++) {
            jobAttachmentResourceIds.push(await insertResource(client, { type: 'JOB_ATTACHMENT', ownerId: null }));
        }

        /* =========================================
         * companies (seed ALL fields except date_created which has default)
         * ========================================= */
        const companyIds = [];
        const domainFor = (name) =>
            name.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '') + '.com';

        for (let i = 0; i < N.companies; i++) {
            const name = faker.company.name();
            const website = `https://www.${domainFor(name)}`;
            const size = pick(['1-10','11-50','51-200','201-500','501-1000','1000+']);
            const description = faker.company.catchPhrase();
            const phone = faker.phone.number('09########');
            const email = `hr@${domainFor(name)}`;
            const industry = pick(INDUSTRIES);
            const maybeLogo = Math.random() < 0.8 ? pick(companyLogoResourceIds) : null;

            const { rows } = await client.query(
                `INSERT INTO companies(
            name, website, size, description, phone, email, industry, logo_resource_id, verified
         )
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
         RETURNING id`,
                [name, website, size, description, phone, email, industry, maybeLogo, Math.random() < 0.6]
            );
            companyIds.push(rows[0].id);
        }

        /* =========================================
         * company_location (HQ + branches)
         * ========================================= */
        for (const cid of companyIds) {
            const k = randInt(1, 3);
            const chosen = faker.helpers.arrayElements(locationIds, k);
            let madeHQ = false;
            for (const lid of chosen) {
                const isHQ = !madeHQ;
                await client.query(
                    `INSERT INTO company_location(company_id, location_id, is_headquarter)
           VALUES ($1, $2, $3)
           ON CONFLICT DO NOTHING`,
                    [cid, lid, isHQ]
                );
                madeHQ = true;
            }
        }

        /* =========================================
         * candidates (seed ALL columns)
         * ========================================= */
        const candidateIds = [];
        const candidateAccountPool = shuffle(accountIds).slice(0, N.candidates);

        if (roleMap['CANDIDATE']) {
            await client.query(
                `UPDATE accounts SET role_id = $1 WHERE id = ANY($2)`,
                [roleMap['CANDIDATE'], candidateAccountPool]
            );
        }

        for (let i = 0; i < candidateAccountPool.length; i++) {
            const accId = candidateAccountPool[i];
            const salaryMin = randInt(400, 2000);
            const salaryMax = salaryMin + randInt(200, 2500);

            const { rows } = await client.query(
                `INSERT INTO candidates(
            account_id, full_name, phone, location_id, seniority,
            salary_expect_min, salary_expect_max, currency,
            remote_pref, relocation_pref, avatar_resource_id, bio
         )
         VALUES ($1, $2, $3, $4, $5::seniority_level,
                 $6, $7, $8,
                 $9, $10, $11, $12)
         ON CONFLICT (account_id) DO NOTHING
         RETURNING id`,
                [
                    accId,
                    faker.person.fullName(),
                    faker.phone.number('09########'),
                    pick(locationIds),
                    pick(ENUM.seniority_level),
                    salaryMin, salaryMax, 'USD',
                    Math.random() < 0.5, Math.random() < 0.3,
                    pick(avatarResourceIds),
                    faker.lorem.sentences(randInt(1, 3)),
                ]
            );

            if (rows[0]) candidateIds.push(rows[0].id);
        }

        /* =========================================
         * recruiters (seed ALL columns; company_id UNIQUE)
         * ========================================= */
        const recruiterCount = Math.min(N.recruiters, companyIds.length);
        const recruiterCompanyList = shuffle(companyIds).slice(0, recruiterCount);
        const recruiterAccountPool = shuffle(
            accountIds.filter((aid) => !candidateAccountPool.includes(aid))
        ).slice(0, recruiterCount);

        if (roleMap['RECRUITER']) {
            await client.query(
                `UPDATE accounts SET role_id = $1 WHERE id = ANY($2)`,
                [roleMap['RECRUITER'], recruiterAccountPool]
            );
        }

        const recruiterIds = [];
        for (let i = 0; i < recruiterCount; i++) {
            const companyId = recruiterCompanyList[i];
            const accId = recruiterAccountPool[i] ?? shuffle(accountIds)[0];

            const { rows } = await client.query(
                `INSERT INTO recruiters(
            account_id, full_name, phone, avatar_resource_id, company_id
         )
         VALUES ($1, $2, $3, $4, $5)
         ON CONFLICT DO NOTHING
         RETURNING id`,
                [
                    accId,
                    faker.person.fullName(),
                    faker.phone.number('09########'),
                    pick(avatarResourceIds),
                    companyId,
                ]
            );

            if (rows[0]) recruiterIds.push(rows[0].id);
        }

        /* =========================================
         * job taxonomy: job_families → sub_families → job_roles
         * ========================================= */
        const familySeeds = [
            'Công nghệ thông tin','Thiết kế','Logistics','Kinh doanh','Kế toán','Nhân sự','Marketing','Sản xuất',
        ];

        const familyIds = [];
        for (const name of faker.helpers.arrayElements(familySeeds, N.jobFamilies)) {
            const slug = faker.helpers.slugify(name.toLowerCase());
            const { rows } = await client.query(
                `INSERT INTO job_families(name, slug)
         VALUES ($1, $2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, slug]
            );
            if (rows[0]) familyIds.push(rows[0].id);
        }

        const subFamilyIds = [];
        for (let i = 0; i < N.subFamilies; i++) {
            const n = cap(faker.word.noun());
            const { rows } = await client.query(
                `INSERT INTO sub_families(name, job_family_id)
         VALUES ($1, $2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [n, pick(familyIds)]
            );
            if (rows[0]) subFamilyIds.push(rows[0].id);
        }

        const roleSeeds = [
            'Software Engineer','Backend Developer','Frontend Developer','DevOps Engineer','QA Engineer',
            'Data Engineer','Data Scientist','AI Engineer','Mobile Developer','Fullstack Developer',
            'Product Manager','UI/UX Designer','Solution Architect','Blockchain Engineer',
        ];

        const jobRoleIds = [];
        for (let i = 0; i < N.jobRoles; i++) {
            const name = `${pick(roleSeeds)}${Math.random() < 0.2 ? ' ' + cap(faker.word.adjective()) : ''}`;
            const { rows } = await client.query(
                `INSERT INTO job_roles(name, sub_family_id)
         VALUES ($1, $2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, pick(subFamilyIds)]
            );
            if (rows[0]) jobRoleIds.push(rows[0].id);
        }

        /* =========================================
         * skills
         * ========================================= */
        const skillSeeds = [
            'Java','Spring Boot','PostgreSQL','Redis','Kafka','Docker','Kubernetes','AWS','GCP','Azure',
            'React','Vue','Angular','Node.js','Python','Django','FastAPI','TensorFlow','PyTorch','Git',
            'CI/CD','Linux','Microservices','GraphQL','gRPC','Elasticsearch','Jenkins','Terraform','Ansible','TypeScript',
            'Go','Rust','C#','.NET','MongoDB','MySQL','RabbitMQ','Nginx','HTML/CSS',
        ];

        const skillIds = [];
        for (const name of faker.helpers.arrayElements(skillSeeds, N.skills)) {
            const aliases = JSON.stringify([name.toLowerCase(), faker.word.sample()]);

            const { rows } = await client.query(
                `INSERT INTO skills(name, aliases)
         VALUES ($1, $2::jsonb)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, aliases]
            );

            if (rows[0]) skillIds.push(rows[0].id);
        }

        /* =========================================
         * jobs + job_description + job_skill_requirements + job_attachments
         * ========================================= */
        const jobIds = [];
        for (let i = 0; i < N.jobs; i++) {
            const companyId = pick(companyIds);
            const title = `${pick(roleSeeds)} ${pick(['I','II','Sr','Lead',''])}`.trim();
            const jobRoleId = pick(jobRoleIds);
            const seniority = pick(ENUM.seniority_level);
            const minExp = randInt(0, 7);
            const locId = Math.random() < 0.7 ? pick(locationIds) : null;
            const workMode = pick(ENUM.work_mode);
            const empType = pick(ENUM.employment_type);
            const smin = randInt(500, 2000);
            const smax = smin + randInt(200, 2000);
            const currency = 'USD';
            const posted = recent(60);
            const expires = soon(randInt(15, 60), posted);
            const status = pick(ENUM.job_status);
            const maxC = Math.random() < 0.5 ? randInt(1, 200) : null;

            const { rows } = await client.query(
                `INSERT INTO jobs(
            company_id, title, job_role_id, seniority, employment_type,
            min_experience_years, location_id, work_mode,
            salary_min, salary_max, currency, max_candidates,
            date_posted, date_expires, status
         )
         VALUES ($1, $2, $3, $4::seniority_level, $5::employment_type,
                 $6, $7, $8::work_mode,
                 $9, $10, $11, $12,
                 $13, $14, $15::job_status)
         RETURNING id`,
                [
                    companyId, title, jobRoleId, seniority, empType,
                    minExp, locId, workMode,
                    smin, smax, currency, maxC,
                    posted, expires, status,
                ]
            );

            const jobId = rows[0].id;
            jobIds.push(jobId);

            // job_description full fields
            const techStackText = skillSeeds.slice(0, randInt(3, 8)).join(', ');
            await client.query(
                `INSERT INTO job_description(
            job_id, summary, responsibilities, requirements,
            nice_to_have, benefits, hiring_process, notes
         )
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8)`,
                [
                    jobId,
                    faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentence(),
                    '- Bảo hiểm, nghỉ phép, team building',
                    '1. CV → 2. Phỏng vấn kỹ thuật → 3. HR',
                    `Tech stack: ${techStackText}`,
                ]
            );

            // yêu cầu kỹ năng
            const reqSkills = faker.helpers.arrayElements(skillIds, randInt(2, 6));
            for (const sid of reqSkills) {
                await client.query(
                    `INSERT INTO job_skill_requirements(job_id, skill_id)
           VALUES ($1, $2)
           ON CONFLICT DO NOTHING`,
                    [jobId, sid]
                );
            }

            // (minh họa) đính kèm JOB_ATTACHMENT (không có bảng join riêng nên chỉ tạo resource)
            // vẫn hữu ích cho demo khi bạn muốn hiển thị file liên quan trong UI
            const attachCount = Math.random() < 0.5 ? randInt(1, 3) : 0;
            for (let k = 0; k < attachCount; k++) {
                await insertResource(client, { type: 'JOB_ATTACHMENT', ownerId: null });
            }
        }

        /* =========================================
         * candidate_skills
         * ========================================= */
        for (const cid of candidateIds) {
            const chosen = faker.helpers.arrayElements(skillIds, randInt(3, 10));
            for (const sid of chosen) {
                await client.query(
                    `INSERT INTO candidate_skills(candidate_id, skill_id, level)
           VALUES ($1, $2, $3)
           ON CONFLICT DO NOTHING`,
                    [cid, sid, randInt(1, 5)]
                );
            }
        }

        /* =========================================
         * job_applications
         * ========================================= */
        const applicationIds = [];
        for (let i = 0; i < N.jobApps; i++) {
            const candidateId = pick(candidateIds);
            const jobId = pick(jobIds);
            const status = pick(ENUM.application_status);
            const appliedAt = recent(45);
            const cvResId = pick(cvResourceIds);

            const { rows } = await client.query(
                `INSERT INTO job_applications(
            candidate_id, job_id, status, cv_resource_id, applied_at
         )
         VALUES ($1, $2, $3::application_status, $4, $5)
         ON CONFLICT (candidate_id, job_id) DO NOTHING
         RETURNING id`,
                [candidateId, jobId, status, cvResId, appliedAt]
            );
            if (rows[0]?.id) applicationIds.push(rows[0].id);
        }

        /* =========================================
         * saved_jobs
         * ========================================= */
        for (let i = 0; i < N.savedJobs; i++) {
            await client.query(
                `INSERT INTO saved_jobs(candidate_id, job_id, saved_at)
         VALUES ($1, $2, $3)
         ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [pick(candidateIds), pick(jobIds), recent(60)]
            );
        }

        /* =========================================
         * interviews (seed ALL columns)
         * - Each interview must have: application_id, scheduled_at, location_id, status, notes
         * ========================================= */
        const interviewCount = Math.min(N.interviews, applicationIds.length);
        for (let i = 0; i < interviewCount; i++) {
            const appId = applicationIds[i];
            const scheduledAt = soon(randInt(1, 20), new Date());
            const locationId = pick(locationIds);
            const status = pick(ENUM.interview_status);
            await client.query(
                `INSERT INTO interviews(
            application_id, scheduled_at, location_id, status, notes
         )
         VALUES ($1, $2, $3, $4::interview_status, $5)`,
                [
                    appId,
                    scheduledAt,
                    locationId,
                    status,
                    faker.lorem.sentence()
                ]
            );
        }

        /* =========================================
         * analytics (INSERT) — explicit JSONB/ENUM casts
         * ========================================= */
        const analyticsRows = [];
        for (let i = 0; i < N.analytics; i++) {
            const eventType = pick(ENUM.analytics_event_type);
            const accId = Math.random() < 0.8 ? pick(accountIds) : null;

            let targetId = null;
            let metadata = {};

            if (eventType === 'SEARCH_QUERY') {
                metadata = {
                    query: faker.lorem.words(3),
                    filters: {
                        seniority: pick(ENUM.seniority_level),
                        province: pick(VN_PROVINCES),
                    },
                };
            } else if (eventType === 'JOB_VIEWED') {
                targetId = pick(jobIds);
                metadata = { dwellMs: randInt(500, 60000) };
            } else if (eventType === 'JOB_APPLIED') {
                targetId = pick(jobIds);
                metadata = { applicationStatus: pick(ENUM.application_status) };
            } else if (eventType === 'JOB_SAVED') {
                targetId = pick(jobIds);
                metadata = {};
            }

            analyticsRows.push([
                accId,
                eventType,
                targetId,
                metadata,
                recent(30),
            ]);
        }
        await insertMany(
            client,
            'analytics',
            ['account_id','event_type','target_id','metadata','occurred_at'],
            analyticsRows,
            ['', '::event_type', '', '::jsonb', '']
        );

        /* =========================================
         * DEMO: BULK UPDATE many analytics rows
         * ========================================= */
        const { rows: aRows } = await client.query(`SELECT id FROM analytics ORDER BY id DESC LIMIT 50`);
        const updates = aRows.map(r => ({
            id: r.id,
            metadata: { updated: true, note: 'bulk-fix', rand: randInt(1, 999) },
            occurred_at: recent(10)
        }));
        const updRes = await updateAnalyticsBulk(client, updates);
        console.log(`ℹ️  Bulk-updated analytics rows: ${updRes.updated}`);

        await client.query('COMMIT');
        console.log('✅ Seed OK!');
    } catch (e) {
        await client.query('ROLLBACK');
        console.error('❌ Seed error:', e);
        process.exitCode = 1;
    } finally {
        client.release();
        await pool.end();
    }
}

main();
