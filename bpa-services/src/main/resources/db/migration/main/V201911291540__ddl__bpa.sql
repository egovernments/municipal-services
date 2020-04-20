CREATE TABLE eg_bpa_BuildingPlan(

id character varying(256),
applicationNo character varying(64),
tenantId character varying(256),
edcrNumber character varying(64),
serviceType character varying(256),
status character varying(64),
additionalDetails JSONB,
ownershipCategory character varying(64),
createdBy character varying(64),
lastModifiedBy character varying(64),
createdTime bigint,
lastModifiedTime bigint,

CONSTRAINT uk_eg_bpa_BuildingPlan UNIQUE (id),
CONSTRAINT pk_eg_bpa_BuildingPlan PRIMARY KEY (id)

);


CREATE TABLE eg_bpa_Unit(
id character varying(64) NOT NULL,
tenantId character varying(256),
usageCategory character varying(64),
buildingPlanId character varying(64),
additionalDetails JSONB,
createdBy character varying(64),
lastModifiedBy character varying(64),
createdTime bigint,
lastModifiedTime bigint,

CONSTRAINT pk_eg_bpa_Unit PRIMARY KEY (id),
CONSTRAINT fk_eg_bpa_Unit FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);


CREATE TABLE eg_bpa_Document(
id character varying(64),
documentType character varying(64),
fileStore character varying(64),
documentUid character varying(64),
buildingPlanId character varying(64),
additionalDetails JSONB,
createdBy character varying(64),
lastModifiedBy character varying(64),
createdTime bigint,
lastModifiedTime bigint,

CONSTRAINT uk_eg_bpa_Document PRIMARY KEY (id),
CONSTRAINT fk_eg_bpa_Document FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);


CREATE TABLE eg_bpa_Address(
id character varying(64),
tenantId character varying(64) NOT NULL,
doorNo character varying(64),
plotNo character varying(64),
landmark character varying(64),
city character varying(64),
district character varying(64),
region character varying(64),
state character varying(64),
country character varying(64),
locality character varying(64),
pincode character varying(64),
additionDetails character varying(64),
buildingName character varying(64),
street character varying(64),
buildingPlanId character varying(64),
createdBy character varying(64),
lastModifiedBy character varying(64),
createdTime bigint,
lastModifiedTime bigint,

CONSTRAINT uk_eg_bpa_Address UNIQUE (id),
CONSTRAINT fk_eg_bpa_Address FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);

CREATE TABLE eg_bpa_owner(

id character varying(64),
tenantId character varying(256) NOT NULL,
uuid character varying(64),
name character varying(256) NOT NULL,
mobileNumber character varying(256) NOT NULL,
gender character varying(256) NOT NULL,
fatherOrHusbandName character varying(256) NOT NULL,
correspondenceAddress character varying(256),
isprimaryowner boolean,
ownershippercentage double precision,
ownertype character varying(64),
institutionId character varying(64),
relationship character varying(64) NOT NULL,
additionalDetails JSONB,
buildingPlanId character varying(64),
createdby character varying(64),
lastmodifiedby character varying(64),
createdtime bigint,
lastmodifiedtime bigint,

CONSTRAINT uk_eg_bpa_owner UNIQUE (id),
CONSTRAINT pk_eg_bpa_owner PRIMARY KEY (id),
CONSTRAINT fk_eg_bpa_owner FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);

CREATE TABLE eg_bpa_GeoLocation(

id character varying(64),
latitude double precision,
longitude double precision,
addressId character varying(64),
additionalDetails JSONB,
createdby character varying(64),
lastmodifiedby character varying(64),
createdtime bigint,
lastmodifiedtime bigint,

CONSTRAINT fk_eg_bpa_GeoLocation FOREIGN KEY (addressId) REFERENCES eg_bpa_Address (id)
);

CREATE TABLE eg_bpa_AuditDetails(
buildingPlanId character varying(64),
createdby character varying(64),
lastmodifiedby character varying(64),
createdtime bigint,
lastmodifiedtime bigint,

CONSTRAINT fk_eg_bpa_AuditDetails FOREIGN KEY (buildingPlanId) REFERENCES eg_bpa_BuildingPlan (id)
);
