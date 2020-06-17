ALTER TABLE eg_ws_meterreading
ADD COLUMN createdBy character varying(64),
ADD COLUMN lastModifiedBy character varying(64),
ADD COLUMN createdTime bigint,
ADD COLUMN lastModifiedTime bigint;