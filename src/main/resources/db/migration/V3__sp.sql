CREATE OR REPLACE FUNCTION get_popular_jobs(
    p_limit INT DEFAULT 20,
    p_recent_days INT DEFAULT 90
)
    RETURNS TABLE (
                      job_id BIGINT,
                      popularity_score DOUBLE PRECISION
                  )
    LANGUAGE plpgsql
AS $$
BEGIN
    -- Lần 1: ưu tiên job mới trong recentDays
    RETURN QUERY
        WITH base_jobs AS (
            SELECT
                j.id,
                j.date_posted,
                EXTRACT(EPOCH FROM (now() - j.date_posted)) / 86400.0 AS age_days
            FROM jobs j
            WHERE j.status = 'PUBLISHED'
              AND (j.date_expires IS NULL OR j.date_expires > now())
              AND j.date_posted IS NOT NULL
              AND j.date_posted >= now() - (p_recent_days || ' days')::interval
        ),
             interaction_agg AS (
                 SELECT
                     ui.job_id,
                     COUNT(*) FILTER (WHERE ui.event_type = 'APPLY') AS apply_count,
                     COUNT(*) FILTER (WHERE ui.event_type = 'SAVE')  AS save_count,
                     COUNT(*) FILTER (
                         WHERE ui.event_type IN (
                                                 'CLICK',
                                                 'CLICK_FROM_SEARCH',
                                                 'CLICK_FROM_SIMILAR',
                                                 'CLICK_FROM_RECOMMENDED'
                             )
                         ) AS click_count
                 FROM user_interactions ui
                 GROUP BY ui.job_id
             ),
             pop AS (
                 SELECT
                     b.id AS job_id,
                     (
                         3.0 * COALESCE(ia.apply_count, 0)::double precision +
                         2.0 * COALESCE(ia.save_count, 0)::double precision +
                         1.0 * COALESCE(ia.click_count, 0)::double precision
                         ) AS popularity_score
                 FROM base_jobs b
                          LEFT JOIN interaction_agg ia ON ia.job_id = b.id
                 ORDER BY popularity_score DESC
                 LIMIT p_limit
             )
        SELECT p.job_id, p.popularity_score
        FROM pop p;

    -- Nếu không có job nào trong recentDays → fallback 1: mọi job active
    IF NOT FOUND THEN
        RETURN QUERY
            WITH base_active AS (
                SELECT j.id
                FROM jobs j
                WHERE j.status = 'PUBLISHED'
                  AND (j.date_expires IS NULL OR j.date_expires > now())
            ),
                 interaction_agg AS (
                     SELECT
                         ui.job_id,
                         COUNT(*) FILTER (WHERE ui.event_type = 'APPLY') AS apply_count,
                         COUNT(*) FILTER (WHERE ui.event_type = 'SAVE')  AS save_count,
                         COUNT(*) FILTER (
                             WHERE ui.event_type IN (
                                                     'CLICK',
                                                     'CLICK_FROM_SEARCH',
                                                     'CLICK_FROM_SIMILAR',
                                                     'CLICK_FROM_RECOMMENDED'
                                 )
                             ) AS click_count
                     FROM user_interactions ui
                     GROUP BY ui.job_id
                 ),
                 pop AS (
                     SELECT
                         b.id AS job_id,
                         (
                             3.0 * COALESCE(ia.apply_count, 0)::double precision +
                             2.0 * COALESCE(ia.save_count, 0)::double precision +
                             1.0 * COALESCE(ia.click_count, 0)::double precision
                             ) AS popularity_score
                     FROM base_active b
                              LEFT JOIN interaction_agg ia ON ia.job_id = b.id
                     ORDER BY popularity_score DESC
                     LIMIT p_limit
                 )
            SELECT p.job_id, p.popularity_score
            FROM pop p;
    END IF;

    -- Nếu vẫn rỗng (ví dụ hệ thống mới, chưa có interaction) → fallback 2: newest jobs
    IF NOT FOUND THEN
        RETURN QUERY
            SELECT
                j.id AS job_id,
                0.5::double precision AS popularity_score
            FROM jobs j
            WHERE j.status = 'PUBLISHED'
              AND (j.date_expires IS NULL OR j.date_expires > now())
            ORDER BY j.date_posted DESC
            LIMIT p_limit;
    END IF;
END;
$$;
