USE empiricus_db;

ALTER TABLE waitlist_entries
    ADD COLUMN email VARCHAR(190) NULL AFTER phone,
    ADD COLUMN `position` INT UNSIGNED NULL AFTER party_size;

UPDATE waitlist_entries w
JOIN (
    SELECT
        id,
        ROW_NUMBER() OVER (
            PARTITION BY restaurant_id
            ORDER BY created_at, id
        ) AS queue_position
    FROM waitlist_entries
    WHERE status = 'WAITING'
) positions ON positions.id = w.id
SET w.`position` = positions.queue_position;

ALTER TABLE waitlist_entries
    MODIFY COLUMN `position` INT UNSIGNED NULL,
    ADD UNIQUE KEY uk_waitlist_restaurant_position
        (restaurant_id, `position`);
