ALTER TABLE eg_bpa_auditdetails
ADD COLUMN IF NOT EXISTS occupancytype character varying(64) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS suboccupancytype character varying(64) DEFAULT NULL,
ADD COLUMN IF NOT EXISTS usages character varying(64) DEFAULT NULL;
