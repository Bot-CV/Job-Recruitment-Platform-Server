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
-- Giả sử mật khẩu là 'admin123'
-- Hash ví dụ bằng BCrypt: $2a$10$7QpK/YL1jYVbF1B0L1N7XOHv84s8Ngh3T.bHDP0cZ1vZTqg2rTduK
-- (Bạn có thể thay lại bằng hash thật của bạn)
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
    '$2a$10$NfCbsSflss4JNAAzS.T0QOa5AvOoNm333IdIyZLlr3UlEr6179kom', -- Pass: Admin@123
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