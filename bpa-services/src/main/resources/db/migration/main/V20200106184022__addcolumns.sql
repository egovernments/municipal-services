ALTER TABLE eg_bpa_buildingplan 
ADD COLUMN holdingNo character varying(64) DEFAULT NULL,
ADD COLUMN registrationDetails character varying(64) DEFAULT NULL,
ADD COLUMN govtOrQuasi character varying(64) DEFAULT NULL,
ALTER COLUMN remarks TYPE character varying(500);

ALTER TABLE eg_bpa_auditdetails 
ADD COLUMN holdingNo character varying(64) DEFAULT NULL,
ADD COLUMN registrationDetails character varying(64) DEFAULT NULL,
ADD COLUMN govtOrQuasi character varying(64) DEFAULT NULL,
ALTER COLUMN remarks TYPE character varying(500);