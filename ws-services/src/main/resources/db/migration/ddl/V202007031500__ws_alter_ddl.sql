
ALTER TABLE eg_ws_connection DROP COLUMN IF EXISTS applicationType, 
DROP COLUMN IF EXISTS dateEffectiveFrom;


ALTER TABLE eg_ws_connection_audit DROP COLUMN IF EXISTS applicationType,
DROP COLUMN IF EXISTS dateEffectiveFrom;
