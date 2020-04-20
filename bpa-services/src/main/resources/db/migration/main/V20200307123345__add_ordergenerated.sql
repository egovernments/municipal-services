ALTER TABLE eg_bpa_buildingplan 
ADD COLUMN orderGeneratedDate bigint;

ALTER TABLE eg_bpa_auditdetails 
ADD COLUMN orderGeneratedDate bigint;
