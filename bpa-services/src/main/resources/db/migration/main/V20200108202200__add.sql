ALTER TABLE eg_bpa_buildingplan 
ADD COLUMN occupancytype character varying(64) DEFAULT NULL,
ADD COLUMN suboccupancytype character varying(64) DEFAULT NULL,
ADD COLUMN usages character varying(64) DEFAULT NULL;

ALTER TABLE eg_bpa_auditdetails
ADD COLUMN occupancytype character varying(64) DEFAULT NULL,
ADD COLUMN suboccupancytype character varying(64) DEFAULT NULL,
ADD COLUMN usages character varying(64) DEFAULT NULL;