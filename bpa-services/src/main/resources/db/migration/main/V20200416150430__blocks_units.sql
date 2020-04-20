ALTER eg_bpa_unit ALTER COLUMN usagecategory TYPE character varying(256);

INSERT INTO eg_bpa_unit(id,buildingplanid,usagecategory,createdby,lastmodifiedby,createdtime,lastmodifiedtime) SELECT id,buildingplanid,suboccupancytype,createdby,lastmodifiedby,createdtime,lastmodifiedtime FROM eg_bpa_blocks;

DROP TABLE IF EXISTS eg_bpa_blocks;
