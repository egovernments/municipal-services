ALTER TABLE eg_bpa_buildingplan 
ADD COLUMN permitOrderNo character varying(64) DEFAULT NULL,
ADD COLUMN remarks character varying(64) DEFAULT NULL ;
ALTER TABLE eg_bpa_auditdetails 
ADD COLUMN permitOrderNo character varying(64) DEFAULT NULL,
ADD COLUMN remarks character varying(64) DEFAULT NULL ;