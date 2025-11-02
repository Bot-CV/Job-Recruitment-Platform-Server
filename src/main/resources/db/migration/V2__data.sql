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