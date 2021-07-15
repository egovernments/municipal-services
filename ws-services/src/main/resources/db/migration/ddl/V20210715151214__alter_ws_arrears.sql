ALTER TABLE eg_ws_connection ADD COLUMN IF NOT EXISTS arrears numeric(12,3);

ALTER TABLE eg_ws_connection ADD COLUMN IF NOT EXISTS previousReadingDate bigint;