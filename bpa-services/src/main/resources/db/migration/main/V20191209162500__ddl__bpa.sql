ALTER TABLE eg_bpa_buildingplan ADD COLUMN applicationtype character varying(64);

ALTER TABLE eg_bpa_buildingplan ALTER COLUMN applicationtype SET NOT NULL;

ALTER TABLE eg_bpa_buildingplan ALTER COLUMN servicetype SET NOT NULL;