ALTER TABLE eg_vehicle_trip ADD COLUMN  fstpEntryTime bigint;
ALTER TABLE eg_vehicle_trip ADD COLUMN  fstpExitTime bigint;
CREATE INDEX  IF NOT EXISTS  index_fstpEntryTime_eg_vehicle_trip  ON eg_vehicle_trip
(    fstpEntryTime
);
CREATE INDEX  IF NOT EXISTS  index_fstpExitTime_eg_vehicle_trip  ON eg_vehicle_trip
(    fstpExitTime
);