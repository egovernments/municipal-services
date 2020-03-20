ALTER TABLE eg_fn_address
    ADD COLUMN areatype character varying(32) not null;

 ALTER TABLE eg_fn_address
    ADD COLUMN subdistrict character varying(64) ;