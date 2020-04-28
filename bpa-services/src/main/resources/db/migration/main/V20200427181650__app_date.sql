ALTER TABLE eg_bpa_unit ADD COLUMN blockindex character varying(64);
UPDATE public.eg_bpa_unit unit
SET tenantid=(SELECT tenantId from eg_bpa_buildingplan  bpa WHERE bpa.id=unit.buildingplanid) WHERE tenantid is null;
UPDATE public.eg_bpa_unit
SET blockIndex=0 WHERE blockIndex is null;

ALTER TABLE eg_bpa_unit
ADD CONSTRAINT eg_bpa_unit_uniqye UNIQUE (id,tenantId,buildingplanid,blockindex);

ALTER TABLE eg_bpa_buildingplan ADD COLUMN applicationDate bigint;