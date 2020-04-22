
UPDATE public.eg_bpa_unit
SET tenantid='bh.sonpur' WHERE tenantid is null;
UPDATE public.eg_bpa_unit
SET blockIndex=0 WHERE blockIndex is null;