ALTER table eg_bpa_owner DROP COLUMN active;

ALTER TABLE eg_bpa_buildingplan ADD COLUMN action character varying(64);