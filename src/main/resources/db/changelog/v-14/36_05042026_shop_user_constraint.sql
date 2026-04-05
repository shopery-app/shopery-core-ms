ALTER TABLE shops DROP CONSTRAINT IF EXISTS shops_user_id_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_one_active_shop_per_user
    ON shops (user_id)
    WHERE status = 'ACTIVE';

CREATE UNIQUE INDEX IF NOT EXISTS uq_one_pending_shop_per_user
    ON shops (user_id)
    WHERE status = 'PENDING';