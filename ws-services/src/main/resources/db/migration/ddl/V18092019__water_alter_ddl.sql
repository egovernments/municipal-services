 ALTER TABLE water_service_connection
 ADD COLUMN proposedpipesize decimal,
 ADD COLUMN proposedTaps integer,
 DROP COLUMN calculationattribute;