ALTER TABLE eg_bpa_unit DROP CONSTRAINT eg_bpa_unit_uniqye;
ALTER TABLE eg_bpa_unit
ADD CONSTRAINT eg_bpa_unit_unique UNIQUE (id,buildingplanid,blockindex);
