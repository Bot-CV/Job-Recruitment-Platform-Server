-- ========================================
-- INIT ROLES (ADMIN, RECRUITER, CANDIDATE)
-- ========================================
INSERT INTO
    roles (name)
SELECT
    'ADMIN'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            roles
        WHERE
            name = 'ADMIN'
    );

INSERT INTO
    roles (name)
SELECT
    'RECRUITER'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            roles
        WHERE
            name = 'RECRUITER'
    );

INSERT INTO
    roles (name)
SELECT
    'CANDIDATE'
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            roles
        WHERE
            name = 'CANDIDATE'
    );

-- ========================================
-- INIT ADMIN ACCOUNT
-- ========================================
-- Email: admin@botcv.com
-- Password: Admin@123
INSERT INTO
    accounts (
        email,
        password,
        role_id,
        status,
        provider,
        verified_at,
        date_created,
        date_updated
    )
SELECT
    'admin@botcv.com',
    '$2a$10$NfCbsSflss4JNAAzS.T0QOa5AvOoNm333IdIyZLlr3UlEr6179kom',
    r.id,
    'ACTIVE',
    'LOCAL',
    NOW (),
    NOW (),
    NOW ()
FROM
    roles r
WHERE
    r.name = 'ADMIN'
    AND NOT EXISTS (
        SELECT
            1
        FROM
            accounts
        WHERE
            email = 'admin@botcv.com'
    );

-- ========================================
-- INIT ADMIN ACCOUNT
-- ========================================
INSERT INTO
    resources (
        mime_type,
        content_type,
        resource_type,
        url,
        public_id,
        name,
        uploaded_at
    )
SELECT
    'image',
    'image/png',
    'AVATAR',
    'https://res.cloudinary.com/dyrppweev/image/upload/v1762334207/user_gtftij.png',
    'bot-cv/avatar/299c1e01-6d3c-462d-a49b-993f51b06cef',
    'default-avatar',
    NOW ()
WHERE
    NOT EXISTS (
        SELECT
            1
        FROM
            resources
        WHERE
            public_id = 'bot-cv/avatar/299c1e01-6d3c-462d-a49b-993f51b06cef'
    );