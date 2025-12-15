CREATE OR REPLACE FUNCTION get_popular_jobs(
    p_limit INT DEFAULT 20,
    p_recent_days INT DEFAULT 90
)
    RETURNS TABLE (
                      job_id BIGINT,
                      popularity_score DOUBLE PRECISION
                  )
    LANGUAGE sql
AS
$$
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
     popular AS (
         SELECT
             b.id AS job_id,
             (
                 3.0 * COALESCE(ia.apply_count, 0)::double precision +
                 2.0 * COALESCE(ia.save_count, 0)::double precision +
                 1.0 * COALESCE(ia.click_count, 0)::double precision
                 ) AS popularity_score
         FROM base_jobs b
                  LEFT JOIN interaction_agg ia ON ia.job_id = b.id
     ),
     popular_ranked AS (
         SELECT
             job_id,
             popularity_score,
             ROW_NUMBER() OVER (ORDER BY popularity_score DESC, job_id) AS rn
         FROM popular
     ),
     newest_fill AS (
         SELECT
             j.id AS job_id,
             0.5::double precision AS popularity_score,
             ROW_NUMBER() OVER (ORDER BY j.date_posted DESC, j.id) AS rn
         FROM jobs j
         WHERE j.status = 'PUBLISHED'
           AND (j.date_expires IS NULL OR j.date_expires > now())
           AND NOT EXISTS (
             SELECT 1
             FROM popular_ranked p
             WHERE p.job_id = j.id
         )
     ),
     max_pop_rank AS (
         SELECT COALESCE(MAX(rn), 0) AS max_rn FROM popular_ranked
     ),
     combined AS (
         -- phần popular trước
         SELECT
             job_id,
             popularity_score,
             rn
         FROM popular_ranked

         UNION ALL

         -- phần fill thêm bằng newest jobs, offset rn để đứng sau popular
         SELECT
             n.job_id,
             n.popularity_score,
             (SELECT max_rn FROM max_pop_rank) + n.rn AS rn
         FROM newest_fill n
     )
SELECT
    job_id,
    popularity_score
FROM combined
ORDER BY rn
LIMIT p_limit;
$$;
