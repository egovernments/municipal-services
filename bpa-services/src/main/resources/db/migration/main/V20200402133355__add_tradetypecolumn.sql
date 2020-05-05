ALTER TABLE eg_bpa_buildingplan
ADD COLUMN tradeType character varying(64) DEFAULT NULL;

ALTER TABLE eg_bpa_auditdetails
ADD COLUMN tradeType character varying(64) DEFAULT NULL;