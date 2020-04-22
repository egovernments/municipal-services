ALTER TABLE eg_bpa_unit
ADD CONSTRAINT eg_bpa_unit_uniqye UNIQUE (id,tenantId,buildingplanid,blockindex);