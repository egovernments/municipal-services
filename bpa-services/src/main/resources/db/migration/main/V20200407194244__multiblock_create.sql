CREATE TABLE eg_bpa_blocks(

id character varying(256),
buildingPlanId character varying(64),
subOccupancyType character varying(256),
createdBy character varying(64),
lastModifiedBy character varying(64),
createdTime bigint,
lastModifiedTime bigint,
    
CONSTRAINT fk_eg_bpa_blocks FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);