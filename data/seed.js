import 'dotenv/config';
import pkg from 'pg';
import { faker } from '@faker-js/faker/locale/vi';

const { Pool } = pkg;

// Allow DATABASE_URL or PG* envs
const pool = new Pool(
    process.env.DATABASE_URL ? { connectionString: process.env.DATABASE_URL } : {}
);

/* ---------- helpers ---------- */
const cap = (s) => (s && s.length ? s.charAt(0).toUpperCase() + s.slice(1) : s);
const pick = (arr) => arr[Math.floor(Math.random() * arr.length)];
const randInt = (min, max) =>
    Math.floor(Math.random() * (max - min + 1)) + min;
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
    seniority_level: [
        'INTERN',
        'FRESHER',
        'JUNIOR',
        'MID',
        'SENIOR',
        'MANAGER',
    ],
    employment_type: [
        'FULL_TIME',
        'PART_TIME',
        'CONTRACT',
        'INTERNSHIP',
        'VOLUNTEER',
        'TEMPORARY',
    ],
    account_status: ['ACTIVE', 'SUSPENDED'],
    auth_provider: ['LOCAL', 'GOOGLE'],
    work_mode: ['ONSITE', 'REMOTE', 'HYBRID'],
    job_status: ['DRAFT', 'PENDING', 'PUBLISHED', 'EXPIRED', 'CANCELED'],
    application_status: [
        'SUBMITTED',
        'REVIEWED',
        'INTERVIEW',
        'OFFERED',
        'REJECTED',
    ],
    resource_type: ['AVATAR', 'CV', 'COMPANY_LOGO'], // DB enum uses 'CV'
    analytics_event_type: [
        'SEARCH_QUERY',
        'JOB_VIEWED',
        'JOB_APPLIED',
        'JOB_SAVED',
    ],
};

/* ---------- sizes ---------- */
const N = {
    roles: 3,
    accounts: 80,

    locations: 60,

    companies: 20,
    recruiters: 15, // <= companies (unique company_id per recruiter)

    candidates: 50,

    jobFamilies: 6,
    subFamilies: 12,
    jobRoles: 30,

    skills: 40,

    jobs: 80,

    jobApps: 120,
    savedJobs: 120,

    avatars: 120, // avatar images for candidates / recruiters
    cvs: 200, // CV files for job applications

    analytics: 200, // random activity events
};

/* ---------- VN geo helpers (đơn giản) ---------- */
const VN_PROVINCES = [
    'Hồ Chí Minh',
    'Hà Nội',
    'Đà Nẵng',
    'Bình Dương',
    'Đồng Nai',
    'Khánh Hòa',
    'Cần Thơ',
    'Hải Phòng',
    'Thừa Thiên Huế',
    'Quảng Ninh',
    'Bắc Ninh',
];

const randomWard = () =>
    Math.random() < 0.5
        ? `Phường ${cap(faker.word.sample())}`
        : `Xã ${cap(faker.word.sample())}`;

const randomDistrict = () =>
    Math.random() < 0.6
        ? `Quận ${randInt(1, 12)}`
        : `Huyện ${cap(faker.word.sample())}`;

const randomStreetAddress = () => {
    const streetName = faker.location.street();
    return `${randInt(1, 299)} ${
        streetName || 'Đường ' + cap(faker.word.sample())
    }`;
};

/* ---------- helpers to insert resources ---------- */
async function insertResource(client, type) {
    const { rows } = await client.query(
        `INSERT INTO resources(mime_type, resource_type, url, public_id, name)
     VALUES ($1,$2,$3,$4,$5)
     RETURNING id`,
        [
            pick(['image/png', 'image/jpeg', 'application/pdf']),
            type, // 'AVATAR' | 'CV' | 'COMPANY_LOGO'
            faker.internet.url(),
            faker.string.uuid(),
            faker.system.fileName(),
        ]
    );
    return rows[0].id;
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
            email,
            password,
            role_id,
            status,
            provider,
            verified_at
         )
         VALUES ($1,$2,$3,$4,$5,$6)
         ON CONFLICT (email) DO NOTHING
         RETURNING id`,
                [
                    email,
                    'password123', // demo only, hashed in real life
                    roleId,
                    pick(ENUM.account_status),
                    provider,
                    verifiedAt,
                ]
            );

            if (rows[0]) {
                accountIds.push(rows[0].id);
            }
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
            const lat = faker.location.latitude({
                min: 8.2,
                max: 23.4,
                precision: 7,
            });
            const lng = faker.location.longitude({
                min: 102.1,
                max: 109.5,
                precision: 7,
            });

            const { rows } = await client.query(
                `INSERT INTO locations(
            street_address,
            ward,
            district,
            province_city,
            country,
            lat,
            lng
         )
         VALUES ($1,$2,$3,$4,$5,$6,$7)
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
            avatarResourceIds.push(await insertResource(client, 'AVATAR'));
        }

        const cvResourceIds = [];
        for (let i = 0; i < N.cvs; i++) {
            cvResourceIds.push(await insertResource(client, 'CV'));
        }

        /* =========================================
         * companies
         * ========================================= */
        const companyIds = [];
        for (let i = 0; i < N.companies; i++) {
            const maybeLogo = Math.random() < 0.6 ? pick(avatarResourceIds) : null; // could also create COMPANY_LOGO pool
            const { rows } = await client.query(
                `INSERT INTO companies(
            name,
            website,
            size,
            logo_resource_id,
            verified,
            description,
            phone
         )
         VALUES ($1,$2,$3,$4,$5,$6,$7)
         RETURNING id`,
                [
                    faker.company.name(),
                    faker.internet.url(),
                    pick([
                        '1-10',
                        '11-50',
                        '51-200',
                        '201-500',
                        '501-1000',
                        '1000+',
                    ]),
                    maybeLogo,
                    Math.random() < 0.6,
                    faker.company.catchPhrase(), // simple desc
                    faker.phone.number('09########'),
                ]
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
                    `INSERT INTO company_location(
              company_id,
              location_id,
              is_headquarter
           )
           VALUES ($1,$2,$3)
           ON CONFLICT DO NOTHING`,
                    [cid, lid, isHQ]
                );
                madeHQ = true;
            }
        }

        /* =========================================
         * candidates
         * NOTE: avatar_resource_id NOT NULL in schema
         * ========================================= */
        const candidateIds = [];

        // pick a bunch of accounts to become candidates
        const candidateAccountPool = shuffle(accountIds).slice(0, N.candidates);

        // force role to CANDIDATE
        if (roleMap['CANDIDATE']) {
            await client.query(
                `UPDATE accounts SET role_id = $1 WHERE id = ANY($2)`,
                [roleMap['CANDIDATE'], candidateAccountPool]
            );
        }

        for (let i = 0; i < candidateAccountPool.length; i++) {
            const accId = candidateAccountPool[i];

            const { rows } = await client.query(
                `INSERT INTO candidates(
            account_id,
            full_name,
            phone,
            location_id,
            seniority,
            salary_expect_min,
            salary_expect_max,
            currency,
            remote_pref,
            relocation_pref,
            avatar_resource_id,
            bio
         )
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)
         ON CONFLICT (account_id) DO NOTHING
         RETURNING id`,
                [
                    accId,
                    faker.person.fullName(),
                    faker.phone.number('09########'),
                    pick(locationIds),
                    pick(ENUM.seniority_level),
                    randInt(400, 2000),
                    randInt(2001, 5000),
                    'USD',
                    Math.random() < 0.5,
                    Math.random() < 0.3,
                    pick(avatarResourceIds),
                    faker.lorem.sentences(randInt(1, 3)),
                ]
            );

            if (rows[0]) {
                candidateIds.push(rows[0].id);
            }
        }

        /* =========================================
         * recruiters
         * - recruiters.company_id UNIQUE in schema
         * - recruiters.account_id UNIQUE in schema
         * ========================================= */
        const recruiterCount = Math.min(N.recruiters, companyIds.length);

        const recruiterCompanyList = shuffle(companyIds).slice(
            0,
            recruiterCount
        );
        const recruiterAccountPool = shuffle(
            accountIds.filter((aid) => !candidateAccountPool.includes(aid))
        ).slice(0, recruiterCount);

        // force role to RECRUITER
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
            account_id,
            full_name,
            phone,
            avatar_resource_id,
            company_id
         )
         VALUES ($1,$2,$3,$4,$5)
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

            if (rows[0]) {
                recruiterIds.push(rows[0].id);
            }
        }

        /* =========================================
         * job taxonomy:
         *   job_families → sub_families → job_roles
         * ========================================= */
        const familySeeds = [
            'Công nghệ thông tin',
            'Thiết kế',
            'Logistics',
            'Kinh doanh',
            'Kế toán',
            'Nhân sự',
            'Marketing',
            'Sản xuất',
        ];

        const familyIds = [];
        for (const name of faker.helpers.arrayElements(
            familySeeds,
            N.jobFamilies
        )) {
            const slug = faker.helpers.slugify(name.toLowerCase());
            const { rows } = await client.query(
                `INSERT INTO job_families(name, slug)
         VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, slug]
            );
            if (rows[0]) {
                familyIds.push(rows[0].id);
            }
        }

        const subFamilyIds = [];
        for (let i = 0; i < N.subFamilies; i++) {
            const n = cap(faker.word.noun());
            const { rows } = await client.query(
                `INSERT INTO sub_families(name, job_family_id)
         VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [n, pick(familyIds)]
            );
            if (rows[0]) {
                subFamilyIds.push(rows[0].id);
            }
        }

        const roleSeeds = [
            'Software Engineer',
            'Backend Developer',
            'Frontend Developer',
            'DevOps Engineer',
            'QA Engineer',
            'Data Engineer',
            'Data Scientist',
            'AI Engineer',
            'Mobile Developer',
            'Fullstack Developer',
            'Product Manager',
            'UI/UX Designer',
            'Solution Architect',
            'Blockchain Engineer',
        ];

        const jobRoleIds = [];
        for (let i = 0; i < N.jobRoles; i++) {
            const name = `${pick(roleSeeds)}${
                Math.random() < 0.2 ? ' ' + cap(faker.word.adjective()) : ''
            }`;
            const { rows } = await client.query(
                `INSERT INTO job_roles(name, sub_family_id)
         VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, pick(subFamilyIds)]
            );
            if (rows[0]) {
                jobRoleIds.push(rows[0].id);
            }
        }

        /* =========================================
         * skills
         * ========================================= */
        const skillSeeds = [
            'Java',
            'Spring Boot',
            'PostgreSQL',
            'Redis',
            'Kafka',
            'Docker',
            'Kubernetes',
            'AWS',
            'GCP',
            'Azure',
            'React',
            'Vue',
            'Angular',
            'Node.js',
            'Python',
            'Django',
            'FastAPI',
            'TensorFlow',
            'PyTorch',
            'Git',
            'CI/CD',
            'Linux',
            'Microservices',
            'GraphQL',
            'gRPC',
            'Elasticsearch',
            'Jenkins',
            'Terraform',
            'Ansible',
            'TypeScript',
            'Go',
            'Rust',
            'C#',
            '.NET',
            'MongoDB',
            'MySQL',
            'RabbitMQ',
            'Nginx',
            'HTML/CSS',
        ];

        const skillIds = [];
        for (const name of faker.helpers.arrayElements(skillSeeds, N.skills)) {
            const aliases = JSON.stringify([
                name.toLowerCase(),
                faker.word.sample(),
            ]);

            const { rows } = await client.query(
                `INSERT INTO skills(name, aliases)
         VALUES ($1,$2)
         ON CONFLICT (name) DO NOTHING
         RETURNING id`,
                [name, aliases]
            );

            if (rows[0]) {
                skillIds.push(rows[0].id);
            }
        }

        /* =========================================
         * jobs + job_description + job_skill_requirements
         * NOTE:
         *   - jobs.max_candidates exists in schema
         *   - jobs.status is NOT NULL
         * ========================================= */
        const jobIds = [];
        for (let i = 0; i < N.jobs; i++) {
            const companyId = pick(companyIds);
            const title = `${pick(roleSeeds)} ${pick(['I', 'II', 'Sr', 'Lead', ''])}`.trim();
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
            company_id,
            title,
            job_role_id,
            seniority,
            employment_type,
            min_experience_years,
            location_id,
            work_mode,
            salary_min,
            salary_max,
            currency,
            max_candidates,
            date_posted,
            date_expires,
            status
         )
         VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15)
         RETURNING id`,
                [
                    companyId,
                    title,
                    jobRoleId,
                    seniority,
                    empType,
                    minExp,
                    locId,
                    workMode,
                    smin,
                    smax,
                    currency,
                    maxC,
                    posted,
                    expires,
                    status,
                ]
            );

            const jobId = rows[0].id;
            jobIds.push(jobId);

            // tạo mô tả job
            const techStackText = skillSeeds
                .slice(0, randInt(3, 8))
                .join(', ');
            await client.query(
                `INSERT INTO job_description(
            job_id,
            summary,
            responsibilities,
            requirements,
            nice_to_have,
            benefits,
            hiring_process,
            notes
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
                    `Tech stack: ${techStackText}`,
                ]
            );

            // yêu cầu kỹ năng
            const reqSkills = faker.helpers.arrayElements(
                skillIds,
                randInt(2, 6)
            );
            for (const sid of reqSkills) {
                await client.query(
                    `INSERT INTO job_skill_requirements(job_id, skill_id)
           VALUES ($1,$2)
           ON CONFLICT DO NOTHING`,
                    [jobId, sid]
                );
            }
        }

        /* =========================================
         * candidate_skills
         * ========================================= */
        for (const cid of candidateIds) {
            const chosen = faker.helpers.arrayElements(
                skillIds,
                randInt(3, 10)
            );
            for (const sid of chosen) {
                await client.query(
                    `INSERT INTO candidate_skills(
              candidate_id,
              skill_id,
              level
           )
           VALUES ($1,$2,$3)
           ON CONFLICT DO NOTHING`,
                    [cid, sid, randInt(1, 5)]
                );
            }
        }

        /* =========================================
         * job_applications
         * (unique per candidate_id + job_id)
         * ========================================= */
        for (let i = 0; i < N.jobApps; i++) {
            const candidateId = pick(candidateIds);
            const jobId = pick(jobIds);
            const status = pick(ENUM.application_status);
            const appliedAt = recent(45);
            const cvResId = pick(cvResourceIds);

            await client.query(
                `INSERT INTO job_applications(
            candidate_id,
            job_id,
            status,
            cv_resource_id,
            applied_at
         )
         VALUES ($1,$2,$3,$4,$5)
         ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [candidateId, jobId, status, cvResId, appliedAt]
            );
        }

        /* =========================================
         * saved_jobs
         * (table now has its own id SERIAL + unique(candidate_id,job_id))
         * ========================================= */
        for (let i = 0; i < N.savedJobs; i++) {
            await client.query(
                `INSERT INTO saved_jobs(
            candidate_id,
            job_id,
            saved_at
         )
         VALUES ($1,$2,$3)
         ON CONFLICT (candidate_id, job_id) DO NOTHING`,
                [pick(candidateIds), pick(jobIds), recent(60)]
            );
        }

        /* =========================================
         * analytics
         * ========================================= */
        for (let i = 0; i < N.analytics; i++) {
            const eventType = pick(ENUM.analytics_event_type);
            const accId =
                Math.random() < 0.8 ? pick(accountIds) : null;

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
                metadata = {
                    applicationStatus: pick(ENUM.application_status),
                };
            } else if (eventType === 'JOB_SAVED') {
                targetId = pick(jobIds);
                metadata = {};
            }

            await client.query(
                `INSERT INTO analytics(
            account_id,
            event_type,
            target_id,
            metadata,
            occurred_at
         )
         VALUES ($1,$2,$3,$4,$5)`,
                [accId, eventType, targetId, metadata, recent(30)]
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
