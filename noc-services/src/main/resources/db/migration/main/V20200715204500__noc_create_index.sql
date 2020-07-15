CREATE INDEX noc_index  ON public.eg_noc 
(
    applicationno,
    nocno,
    tenantid,
    id,
    applicationstatus,
    noctype
);
