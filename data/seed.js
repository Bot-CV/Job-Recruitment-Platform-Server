// seed.js (ESM). Nếu dự án của bạn chưa dùng ESM, đổi 2 dòng import thành require(...) theo CommonJS.

import 'dotenv/config';
import pkg from 'pg';
import { faker } from '@faker-js/faker/locale/vi';

const { Pool } = pkg;
const pool = new Pool(
    process.env.DATABASE_URL ? { connectionString: process.env.DATABASE_URL } : {}
);

/* =========================
 * Helpers
 * ========================= */
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

/* =========================
 * ENUMS (phải khớp DB)
 * ========================= */
const ENUM = {
    seniority_level: ['INTERN','FRESHER','JUNIOR','MID','SENIOR','MANAGER'],
    employment_type: ['FULL_TIME','PART_TIME','CONTRACT','INTERNSHIP','VOLUNTEER','TEMPORARY'],
    account_status: ['ACTIVE','SUSPENDED'],
    auth_provider: ['LOCAL','GOOGLE'],
    work_mode: ['ONSITE','REMOTE','HYBRID'],
    job_status: ['DRAFT','PENDING','PUBLISHED','EXPIRED','CANCELED'],
    application_status: ['SUBMITTED','REVIEWED','INTERVIEW','OFFERED','REJECTED'],
    resource_type: ['AVATAR','CV','COMPANY_LOGO','JOB_ATTACHMENT'],
    interview_status: ['SCHEDULED','COMPLETED','CANCELED','NO_SHOW'],
};

/* =========================
 * Kích thước dataset
 * ========================= */
const N = {
    roles: 3,
    accounts: 90,
    locations: 60,
    companies: 24,
    recruiters: 20,        // ≤ companies, do company_id UNIQUE
    candidates: 60,

    jobFamilies: 8,
    subFamilies: 16,
    jobRoles: 36,

    skills: 50,

    jobs: 90,

    jobApps: 150,
    savedJobs: 160,

    avatars: 200,
    cvs: 240,
    companyLogos: 60,
    jobAttachments: 160,

    interviews: 100,

    attestationsPerCompany: [0, 2], // min, max số tài liệu xác thực
};

/* =========================
 * VN geo helpers
 * ========================= */
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

/* =========================
 * insertMany tiện ích (hỗ trợ CAST)
 * ========================= */
async function insertMany(client, table, columns, rows, casts = []) {
    if (!rows.length) return;
    const colList = columns.map(c => `"${c}"`).join(', ');
    const values = [];
    let idx = 1;
    const placeholders = rows.map(row => {
        const parts = row.map((val, j) => {
            const cast = casts[j] || '';
            const v = (typeof val === 'object' && val !== null && !(val instanceof Date))
                ? JSON.stringify(val)
                : val;
            values.push(v);
            return `$${idx++}${cast}`;
        });
        return `(${parts.join(', ')})`;
    }).join(', ');
    const sql = `INSERT INTO ${table} (${colList}) VALUES ${placeholders}`;
    await client.query(sql, values);
}

/* =========================
 * Resource creator (có content_type)
 * ========================= */
function guessContentType(mime) {
    return (mime || '').split('/')[0] || 'application';
}
async function insertResource(client, { type, ownerId = null, mime = null }) {
    const mimeType = mime || pick(['image/png', 'image/jpeg', 'application/pdf']);
    const contentType = guessContentType(mimeType);
    const { rows } = await client.query(
        `INSERT INTO resources(
            owner_id,
            mime_type,
            content_type,
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
            contentType,
            type,
            faker.internet.url(),
            faker.string.uuid(),
            faker.system.fileName(),
        ]
    );
    return rows[0].id;
}


/* =========================
 * MAIN
 * ========================= */
async function main() {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        /* -------- roles -------- */
        const roleNames = ['ADMIN','RECRUITER','CANDIDATE'].slice(0, N.roles);
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
            roleIds.push(rows[0].id);
            roleMap[name] = rows[0].id;
        }

        /* -------- accounts -------- */
        const accountIds = [];
        const emails = new Set();
        for (let i = 0; i < N.accounts; i++) {
            let email;
            do { email = faker.internet.email().toLowerCase(); } while (emails.has(email));
            emails.add(email);
            const provider = pick(ENUM.auth_provider);
            const verifiedAt = Math.random() < 0.7 ? recent(180) : null;
            const roleId = pick(roleIds);
            const { rows } = await client.query(
                `INSERT INTO accounts(
                    email, password, role_id, status, provider, verified_at
                )
                 VALUES ($1, $2, $3, $4::account_status, $5::auth_provider, $6)
                     ON CONFLICT (email) DO NOTHING
         RETURNING id`,
                [email, 'password123', roleId, pick(ENUM.account_status), provider, verifiedAt]
            );
            if (rows[0]?.id) accountIds.push(rows[0].id);
        }

        /* -------- locations -------- */
        const locationIds = [];
        for (let i = 0; i < N.locations; i++) {
            const { rows } = await client.query(
                `INSERT INTO locations(
                    street_address, ward, district, province_city, country, lat, lng
                )
                 VALUES ($1,$2,$3,$4,$5,$6,$7)
                     RETURNING id`,
                [
                    randomStreetAddress(), randomWard(), randomDistrict(),
                    pick(VN_PROVINCES), 'Việt Nam',
                    faker.location.latitude({ min: 8.2, max: 23.4, precision: 7 }),
                    faker.location.longitude({ min: 102.1, max: 109.5, precision: 7 }),
                ]
            );
            locationIds.push(rows[0].id);
        }

        /* -------- resource pools -------- */
        const avatarResourceIds = [];
        for (let i = 0; i < N.avatars; i++) {
            avatarResourceIds.push(await insertResource(client, { type: 'AVATAR', ownerId: null, mime: pick(['image/png','image/jpeg']) }));
        }
        const cvResourceIds = [];
        for (let i = 0; i < N.cvs; i++) {
            cvResourceIds.push(await insertResource(client, { type: 'CV', ownerId: null, mime: 'application/pdf' }));
        }
        const companyLogoResourceIds = [];
        for (let i = 0; i < N.companyLogos; i++) {
            companyLogoResourceIds.push(await insertResource(client, { type: 'COMPANY_LOGO', ownerId: null, mime: pick(['image/png','image/jpeg']) }));
        }
        const jobAttachmentResourceIds = [];
        for (let i = 0; i < N.jobAttachments; i++) {
            jobAttachmentResourceIds.push(await insertResource(client, { type: 'JOB_ATTACHMENT', ownerId: null, mime: 'application/pdf' }));
        }

        /* -------- companies (đủ cột) -------- */
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
            const logoId = Math.random() < 0.85 ? pick(companyLogoResourceIds) : null;
            const { rows } = await client.query(
                `INSERT INTO companies(
                    name, website, size, description, phone, email, industry, logo_resource_id, verified
                )
                 VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)
                     RETURNING id`,
                [name, website, size, description, phone, email, industry, logoId, Math.random() < 0.6]
            );
            companyIds.push(rows[0].id);
        }

        /* -------- company_location -------- */
        for (const cid of companyIds) {
            const k = randInt(1, 3);
            const chosen = faker.helpers.arrayElements(locationIds, k);
            let madeHQ = false;
            for (const lid of chosen) {
                const isHQ = !madeHQ;
                await client.query(
                    `INSERT INTO company_location(company_id, location_id, is_headquarter)
                     VALUES ($1,$2,$3)
                         ON CONFLICT DO NOTHING`,
                    [cid, lid, isHQ]
                );
                madeHQ = true;
            }
        }

        /* -------- attestation_resources (link company ↔ resource) -------- */
        // Dùng resource type JOB_ATTACHMENT (PDF) như giấy tờ xác thực công ty
        for (const cid of companyIds) {
            const num = randInt(N.attestationsPerCompany[0], N.attestationsPerCompany[1]);
            const chosen = faker.helpers.arrayElements(jobAttachmentResourceIds, num);
            for (const rid of chosen) {
                await client.query(
                    `INSERT INTO attestation_resources(company_id, resource_id)
                     VALUES ($1,$2)
                         ON CONFLICT DO NOTHING`,
                    [cid, rid]
                );
            }
        }

        /* -------- candidates (đủ cột) -------- */
        const candidateIds = [];
        const candidateAccountPool = shuffle(accountIds).slice(0, N.candidates);
        if (roleMap['CANDIDATE']) {
            await client.query(`UPDATE accounts SET role_id = $1 WHERE id = ANY($2)`, [roleMap['CANDIDATE'], candidateAccountPool]);
        }
        for (const accId of candidateAccountPool) {
            const salaryMin = randInt(400, 2000);
            const salaryMax = salaryMin + randInt(200, 2500);
            const { rows } = await client.query(
                `INSERT INTO candidates(
                    account_id, full_name, phone, location_id, seniority,
                    salary_expect_min, salary_expect_max, currency,
                    remote_pref, relocation_pref, avatar_resource_id, bio
                )
                 VALUES ($1,$2,$3,$4,$5::seniority_level,
                         $6,$7,$8,
                         $9,$10,$11,$12)
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
            if (rows[0]?.id) candidateIds.push(rows[0].id);
        }

        /* -------- recruiters (company_id UNIQUE) -------- */
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
            const accId = recruiterAccountPool[i];
            const companyId = recruiterCompanyList[i];
            const { rows } = await client.query(
                `INSERT INTO recruiters(
                    account_id, full_name, phone, avatar_resource_id, company_id
                )
                 VALUES ($1,$2,$3,$4,$5)
                     ON CONFLICT DO NOTHING
         RETURNING id`,
                [
                    accId,
                    faker.person.fullName(),
                    faker.phone.number('09########'),
                    pick(avatarResourceIds),
                    companyId
                ]
            );
            if (rows[0]?.id) recruiterIds.push(rows[0].id);
        }

        /* -------- taxonomy -------- */
        const familySeeds = [
            'Công nghệ thông tin','Thiết kế','Logistics','Kinh doanh','Kế toán','Nhân sự','Marketing','Sản xuất',
        ];
        const familyIds = [];
        for (const name of faker.helpers.arrayElements(familySeeds, N.jobFamilies)) {
            const slug = faker.helpers.slugify(name.toLowerCase());
            const { rows } = await client.query(
                `INSERT INTO job_families(name, slug)
                 VALUES ($1,$2)
                     ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, slug]
            );
            if (rows[0]?.id) familyIds.push(rows[0].id);
        }

        const subFamilyIds = [];
        for (let i = 0; i < N.subFamilies; i++) {
            const { rows } = await client.query(
                `INSERT INTO sub_families(name, job_family_id)
                 VALUES ($1,$2)
                     ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [cap(faker.word.noun()), pick(familyIds)]
            );
            if (rows[0]?.id) subFamilyIds.push(rows[0].id);
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
                 VALUES ($1,$2)
                     ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, pick(subFamilyIds)]
            );
            if (rows[0]?.id) jobRoleIds.push(rows[0].id);
        }

        /* -------- skills -------- */
        const skillSeeds = [
            'Java','Spring Boot','PostgreSQL','Redis','Kafka','Docker','Kubernetes','AWS','GCP','Azure',
            'React','Vue','Angular','Node.js','Python','Django','FastAPI','TensorFlow','PyTorch','Git',
            'CI/CD','Linux','Microservices','GraphQL','gRPC','Elasticsearch','Jenkins','Terraform','Ansible','TypeScript',
            'Go','Rust','C#','.NET','MongoDB','MySQL','RabbitMQ','Nginx','HTML/CSS',
        ];
        const skillIds = [];
        for (const name of faker.helpers.arrayElements(skillSeeds, N.skills)) {
            const aliases = [name.toLowerCase(), faker.word.sample()];
            const { rows } = await client.query(
                `INSERT INTO skills(name, aliases)
                 VALUES ($1,$2::jsonb)
                     ON CONFLICT (name) DO NOTHING
   RETURNING id`,
                [name, JSON.stringify(aliases)] // <-- OK: là JSON hợp lệ
            );
            if (rows[0]?.id) skillIds.push(rows[0].id);
        }

        /* -------- jobs + description + skill requirements + attachments -------- */
        const jobIds = [];
        for (let i = 0; i < N.jobs; i++) {
            const companyId = pick(companyIds);
            const title = `${pick(roleSeeds)} ${pick(['I','II','Sr','Lead',''])}`.trim();
            const jobRoleId = pick(jobRoleIds);
            const seniority = pick(ENUM.seniority_level);
            const empType = pick(ENUM.employment_type);
            const minExp = randInt(0, 7);
            const locId = Math.random() < 0.7 ? pick(locationIds) : null;
            const workMode = pick(ENUM.work_mode);
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
                 VALUES ($1,$2,$3,$4::seniority_level,$5::employment_type,
                         $6,$7,$8::work_mode,
                         $9,$10,$11,$12,
                         $13,$14,$15::job_status)
                     RETURNING id`,
                [
                    companyId, title, jobRoleId, seniority, empType,
                    minExp, locId, workMode,
                    smin, smax, currency, maxC,
                    posted, expires, status
                ]
            );
            const jobId = rows[0].id;
            jobIds.push(jobId);

            await client.query(
                `INSERT INTO job_description(
                    job_id, summary, responsibilities, requirements,
                    nice_to_have, benefits, hiring_process, notes
                )
                 VALUES ($1,$2,$3,$4,$5,$6,$7,$8)`,
                [
                    jobId,
                    faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentence(),
                    '- Bảo hiểm, nghỉ phép, team building',
                    '1. CV → 2. Phỏng vấn kỹ thuật → 3. HR',
                    `Tech stack: ${skillSeeds.slice(0, randInt(3, 8)).join(', ')}`
                ]
            );

            const reqSkills = faker.helpers.arrayElements(skillIds, randInt(2, 6));
            for (const sid of reqSkills) {
                await client.query(
                    `INSERT INTO job_skill_requirements(job_id, skill_id)
                     VALUES ($1,$2)
                         ON CONFLICT DO NOTHING`,
                    [jobId, sid]
                );
            }

            // đính kèm giả lập (resource JOB_ATTACHMENT)
            const attachCount = Math.random() < 0.5 ? randInt(1, 3) : 0;
            for (let k = 0; k < attachCount; k++) {
                await insertResource(client, { type: 'JOB_ATTACHMENT', ownerId: null, mime: 'application/pdf' });
            }
        }

        /* -------- candidate_skills -------- */
        for (const cid of candidateIds) {
            const chosen = faker.helpers.arrayElements(skillIds, randInt(3, 10));
            for (const sid of chosen) {
                await client.query(
                    `INSERT INTO candidate_skills(candidate_id, skill_id, level)
                     VALUES ($1,$2,$3)
                         ON CONFLICT DO NOTHING`,
                    [cid, sid, randInt(1, 5)]
                );
            }
        }

        /* -------- job_applications -------- */
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
                 VALUES ($1,$2,$3::application_status,$4,$5)
                     ON CONFLICT (candidate_id, job_id) DO NOTHING
         RETURNING id`,
                [candidateId, jobId, status, cvResId, appliedAt]
            );
            if (rows[0]?.id) applicationIds.push(rows[0].id);
        }

        /* -------- saved_jobs -------- */
        for (let i = 0; i < N.savedJobs; i++) {
            await client.query(
                `INSERT INTO saved_jobs(candidate_id, job_id, saved_at)
                 VALUES ($1,$2,$3)
                     ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [pick(candidateIds), pick(jobIds), recent(60)]
            );
        }

        /* -------- interviews (đủ cột) -------- */
        const interviewCount = Math.min(N.interviews, applicationIds.length);
        for (let i = 0; i < interviewCount; i++) {
            const appId = applicationIds[i];
            await client.query(
                `INSERT INTO interviews(
                    application_id, scheduled_at, location_id, status, notes
                )
                 VALUES ($1,$2,$3,$4::interview_status,$5)`,
                [
                    appId,
                    soon(randInt(1, 20), new Date()),
                    pick(locationIds),
                    pick(ENUM.interview_status),
                    faker.lorem.sentence()
                ]
            );
        }


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
