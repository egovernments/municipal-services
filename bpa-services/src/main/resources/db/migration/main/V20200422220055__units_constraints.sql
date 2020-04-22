UPDATE public.eg_bpa_unit
SET tenantid='bh.sonpur' WHERE tenantid is null;
UPDATE public.eg_bpa_unit
SET blockIndex=0 WHERE blockIndex is null;

ALTER TABLE eg_bpa_unit
ADD CONSTRAINT eg_bpa_unit_uniqye UNIQUE (id,tenantId,buildingplanid,blockindex);
