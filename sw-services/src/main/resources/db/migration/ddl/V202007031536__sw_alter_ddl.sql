ALTER TABLE eg_sw_connection
ADD COLUMN applicationType character varying(64),
ADD COLUMN dateEffectiveFrom bigint;

ALTER TABLE eg_sw_connection_audit
ADD COLUMN applicationType character varying(64),
ADD COLUMN dateEffectiveFrom bigint;
