ALTER TABLE eg_bpa_owner ALTER COLUMN uuid TYPE character varying(256);

ALTER TABLE eg_bpa_owner ALTER COLUMN id TYPE BIGINT USING id::bigint;