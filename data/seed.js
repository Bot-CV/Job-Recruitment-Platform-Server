import 'dotenv/config';
import pkg from 'pg';
import { faker } from '@faker-js/faker/locale/vi';

const { Pool } = pkg;

// Cho phép dùng DATABASE_URL hoặc các biến PGHOST/PGUSER/PGPASSWORD...
const pool = new Pool(process.env.DATABASE_URL ? { connectionString: process.env.DATABASE_URL } : {});

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

/* ---------- enums ---------- */
const ENUM = {
    seniority_level: ['INTERN','FRESHER','JUNIOR','MID','SENIOR','MANAGER'],
    account_status: ['ACTIVE','SUSPENDED'],
    auth_provider: ['LOCAL','GOOGLE'],
    work_mode: ['ONSITE','REMOTE','HYBRID'],
    job_status: ['DRAFT','PUBLISHED','PAUSED','EXPIRED'],
    application_status: ['SUBMITTED','REVIEWED','INTERVIEW','OFFERED','REJECTED'],
    resource_type: ['AVATAR','CV', 'COMPANY_LOGO'],
};

/* ---------- sizes ---------- */
const N = {
    roles: 3,
    accounts: 80,
    locations: 60,
    companies: 20,
    candidates: 50,
    recruiters: 15, // sẽ tự co lại nếu > companies
    jobFamilies: 6,
    subFamilies: 12,
    jobRoles: 30,
    skills: 40,
    jobs: 80,
    jobApps: 120,
    savedJobs: 120,
    resources: 150,
};

/* ---------- VN geo helpers (đơn giản) ---------- */
const VN_PROVINCES = [
    'Hồ Chí Minh','Hà Nội','Đà Nẵng','Bình Dương','Đồng Nai',
    'Khánh Hòa','Cần Thơ','Hải Phòng','Thừa Thiên Huế','Quảng Ninh','Bắc Ninh'
];
const randomWard = () =>
    (Math.random() < 0.5 ? `Phường ${cap(faker.word.sample())}` : `Xã ${cap(faker.word.sample())}`);
const randomDistrict = () =>
    (Math.random() < 0.6 ? `Quận ${randInt(1, 12)}` : `Huyện ${cap(faker.word.sample())}`);
const randomStreetAddress = () => {
    const streetName = faker.location.street(); // tên đường
    return `${randInt(1, 299)} ${streetName || 'Đường ' + cap(faker.word.sample())}`;
};

async function main() {
    const client = await pool.connect();
    try {
        await client.query('BEGIN');

        /* ---- roles ---- */
        const roleNames = ['ADMIN', 'RECRUITER', 'CANDIDATE'].slice(0, N.roles);
        const roleIds = [];
        for (const name of roleNames) {
            const { rows } = await client.query(
                `INSERT INTO roles(name) VALUES ($1)
         ON CONFLICT (name) DO UPDATE SET name = EXCLUDED.name
         RETURNING id`,
                [name]
            );
            roleIds.push(rows[0].id);
        }

        /* ---- accounts ---- */
        const accountIds = [];
        const emails = new Set();
        for (let i = 0; i < N.accounts; i++) {
            let email;
            do email = faker.internet.email().toLowerCase(); while (emails.has(email));
            emails.add(email);

            const roleId = pick(roleIds);
            const provider = pick(ENUM.auth_provider);
            const verifiedAt = Math.random() < 0.7 ? recent(120) : null;

            const { rows } = await client.query(
                `INSERT INTO accounts(email,password,role_id,status,provider,verified_at)
         VALUES ($1,$2,$3,$4,$5,$6)
         ON CONFLICT (email) DO NOTHING
         RETURNING id`,
                [email, 'password123', roleId, pick(ENUM.account_status), provider, verifiedAt]
            );
            if (rows[0]) accountIds.push(rows[0].id);
        }

        /* ---- locations ---- */
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
                `INSERT INTO locations(street_address, ward, district, province_city, country, lat, lng)
         VALUES ($1,$2,$3,$4,$5,$6,$7)
         RETURNING id`,
                [street, ward, district, province, country, lat, lng]
            );
            locationIds.push(rows[0].id);
        }

        /* ---- companies ---- */
        const companyIds = [];
        for (let i = 0; i < N.companies; i++) {
            const { rows } = await client.query(
                `INSERT INTO companies(name, website, size, logo_resource_id, verified)
         VALUES ($1,$2,$3,$4,$5)
         RETURNING id`,
                [
                    faker.company.name(),
                    faker.internet.url(),
                    pick(['1-10','11-50','51-200','201-500','501-1000','1000+']),
                    null,
                    Math.random() < 0.6
                ]
            );
            companyIds.push(rows[0].id);
        }

        /* ---- company_location ---- */
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

        /* ---- candidates ---- */
        const candidateIds = [];
        const candidateAccountPool = shuffle(accountIds).slice(0, N.candidates); // mỗi candidate 1 account khác nhau
        for (let i = 0; i < candidateAccountPool.length; i++) {
            const accId = candidateAccountPool[i];
            const { rows } = await client.query(
                `INSERT INTO candidates(account_id, full_name, location_id, seniority,
                                salary_expect_min, salary_expect_max, currency,
                                remote_pref, relocation_pref, avatar_resource_id, bio)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)
         ON CONFLICT (account_id) DO NOTHING
         RETURNING id`,
                [
                    accId,
                    faker.person.fullName(),
                    pick(locationIds),
                    pick(ENUM.seniority_level),
                    randInt(400, 2000),
                    randInt(2001, 5000),
                    'USD',
                    Math.random() < 0.5,
                    Math.random() < 0.3,
                    randInt(1, 100000),
                    faker.lorem.sentences(randInt(1, 3))
                ]
            );
            if (rows[0]) candidateIds.push(rows[0].id);
        }

        /* ---- recruiters (company_id phải duy nhất) ---- */
        const recruiterCount = Math.min(N.recruiters, companyIds.length);
        const recruiterCompanyList = shuffle(companyIds).slice(0, recruiterCount); // không trùng company
        const recruiterAccountPool = shuffle(
            accountIds.filter(aid => !candidateAccountPool.includes(aid))
        ).slice(0, recruiterCount); // tránh đụng accounts đã dùng cho candidate

        const recruiterIds = [];
        for (let i = 0; i < recruiterCount; i++) {
            const companyId = recruiterCompanyList[i];
            const accId = recruiterAccountPool[i] ?? shuffle(accountIds)[0]; // fallback nếu thiếu

            const { rows } = await client.query(
                `INSERT INTO recruiters(account_id, full_name, avatar_resource_id, company_id)
         VALUES ($1,$2,$3,$4)
         ON CONFLICT DO NOTHING
         RETURNING id`,
                [accId, faker.person.fullName(), randInt(1, 100000), companyId]
            );
            if (rows[0]) recruiterIds.push(rows[0].id);
        }

        /* ---- taxonomy: families → sub_families → roles ---- */
        const familySeeds = ['Công nghệ thông tin','Thiết kế','Logistics','Kinh doanh','Kế toán','Nhân sự','Marketing','Sản xuất'];
        const familyIds = [];
        for (const name of faker.helpers.arrayElements(familySeeds, N.jobFamilies)) {
            const slug = faker.helpers.slugify(name.toLowerCase());
            const { rows } = await client.query(
                `INSERT INTO job_families(name, slug) VALUES ($1,$2)
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
                `INSERT INTO sub_families(name, job_family_id) VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [n, pick(familyIds)]
            );
            if (rows[0]) subFamilyIds.push(rows[0].id);
        }

        const roleSeeds = [
            'Software Engineer','Backend Developer','Frontend Developer','DevOps Engineer','QA Engineer',
            'Data Engineer','Data Scientist','AI Engineer','Mobile Developer','Fullstack Developer',
            'Product Manager','UI/UX Designer','Solution Architect','Blockchain Engineer'
        ];
        const jobRoleIds = [];
        for (let i = 0; i < N.jobRoles; i++) {
            const name = `${pick(roleSeeds)}${Math.random() < 0.2 ? ' ' + cap(faker.word.adjective()) : ''}`;
            const { rows } = await client.query(
                `INSERT INTO job_roles(name, sub_family_id) VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, pick(subFamilyIds)]
            );
            if (rows[0]) jobRoleIds.push(rows[0].id);
        }

        /* ---- skills ---- */
        const skillSeeds = [
            'Java','Spring Boot','PostgreSQL','Redis','Kafka','Docker','Kubernetes','AWS','GCP','Azure',
            'React','Vue','Angular','Node.js','Python','Django','FastAPI','TensorFlow','PyTorch','Git',
            'CI/CD','Linux','Microservices','GraphQL','gRPC','Elasticsearch','Jenkins','Terraform','Ansible',
            'TypeScript','Go','Rust','C#','.NET','MongoDB','MySQL','RabbitMQ','Nginx','HTML/CSS'
        ];
        const skillIds = [];
        for (const name of faker.helpers.arrayElements(skillSeeds, N.skills)) {
            const aliases = JSON.stringify([name.toLowerCase(), faker.word.sample()]);
            const { rows } = await client.query(
                `INSERT INTO skills(name, aliases) VALUES ($1, $2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, aliases]
            );
            if (rows[0]) skillIds.push(rows[0].id);
        }

        /* ---- jobs + descriptions + required skills ---- */
        const jobIds = [];
        for (let i = 0; i < N.jobs; i++) {
            const companyId = pick(companyIds);
            const title = `${pick(roleSeeds)} ${pick(['I','II','Sr','Lead',''])}`.trim();
            const jobRoleId = pick(jobRoleIds);
            const seniority = pick(ENUM.seniority_level);
            const minExp = randInt(0, 7);
            const locId = Math.random() < 0.7 ? pick(locationIds) : null;
            const workMode = pick(ENUM.work_mode);
            const smin = randInt(500, 2000);
            const smax = smin + randInt(200, 2000);
            const currency = 'USD';
            const posted = recent(60);
            const expires = soon(randInt(15, 60), posted);
            const status = pick(ENUM.job_status);

            const { rows } = await client.query(
                `INSERT INTO jobs(company_id, title, job_role_id, seniority, min_experience_years, location_id,
                          work_mode, salary_min, salary_max, currency, date_posted, date_expires, status)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13)
         RETURNING id`,
                [companyId, title, jobRoleId, seniority, minExp, locId, workMode, smin, smax, currency, posted, expires, status]
            );
            const jobId = rows[0].id;
            jobIds.push(jobId);

            await client.query(
                `INSERT INTO job_description(job_id, summary, responsibilities, requirements, nice_to_have, benefits, tech_stack, hiring_process, notes)
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9)`,
                [
                    jobId,
                    faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentences(2),
                    '- ' + faker.lorem.sentence(),
                    '- Bảo hiểm, nghỉ phép, team building',
                    skillSeeds.slice(0, randInt(3, 8)).join(', '),
                    '1. CV → 2. Phỏng vấn kỹ thuật → 3. HR',
                    faker.lorem.sentence()
                ]
            );

            const reqSkills = faker.helpers.arrayElements(skillIds, randInt(2, 6));
            for (const sid of reqSkills) {
                await client.query(
                    `INSERT INTO job_skill_requirements(job_id, skill_id, level)
           VALUES ($1,$2,$3)
           ON CONFLICT DO NOTHING`,
                    [jobId, sid, randInt(2, 5)]
                );
            }
        }

        /* ---- candidate skills ---- */
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

        /* ---- job applications ---- */
        for (let i = 0; i < N.jobApps; i++) {
            const candidateId = pick(candidateIds);
            const jobId = pick(jobIds);
            const status = pick(ENUM.application_status);
            const appliedAt = recent(45);

            await client.query(
                `INSERT INTO job_applications(candidate_id, job_id, status, cv_resource_id, applied_at)
         VALUES ($1,$2,$3,$4,$5)
         ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [candidateId, jobId, status, randInt(1, 100000), appliedAt]
            );
        }

        /* ---- saved jobs ---- */
        for (let i = 0; i < N.savedJobs; i++) {
            await client.query(
                `INSERT INTO saved_jobs(candidate_id, job_id, saved_at)
         VALUES ($1,$2,$3)
         ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [pick(candidateIds), pick(jobIds), recent(60)]
            );
        }

        /* ---- resources ---- */
        for (let i = 0; i < N.resources; i++) {
            await client.query(
                `INSERT INTO resources(mime_type, owner_id, owner_type, url, name)
         VALUES ($1,$2,$3,$4,$5)`,
                [
                    pick(['image/png','image/jpeg','application/pdf']),
                    Math.random() < 0.5 ? pick(candidateIds) : pick(recruiterIds.length ? recruiterIds : candidateIds),
                    pick(ENUM.resource_type),
                    faker.internet.url(),
                    faker.system.fileName()
                ]
            );
        }

        await client.query('COMMIT');
        console.log('✅ Seed OK!');
    } catch (e) {
        await client.query('ROLLBACK');
        console.error('❌ Seed error:', e);
    } finally {
        client.release();
        await pool.end();
    }
}

main();
