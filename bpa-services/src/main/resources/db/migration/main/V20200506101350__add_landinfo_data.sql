CREATE TABLE eg_land_landInfo(
	id character varying(64),
	landUid character varying(64),
	landUniqueRegNo character varying(64),
	tenantId character varying(256) NOT NULL,
	status character varying(64),
	ownershipCategory character varying(64),
	source character varying(64),
	channel character varying(64),
	additionalDetails JSONB,
	
	CONSTRAINT uk_eg_land_landInfo UNIQUE (id),
	CONSTRAINT pk_eg_land_landInfo PRIMARY KEY (id)
);

CREATE TABLE eg_land_Address(
	id character varying(64),
	tenantId character varying(256) NOT NULL,
	doorNo character varying(64),
	plotNo character varying(64),
	landmark character varying(64),
	city character varying(64),
	district character varying(64),
	region character varying(64),
	state character varying(64),
	country character varying(64),
	pincode character varying(64),
	additionDetails character varying(64),
	buildingName character varying(64),
	street character varying(64),
	landInfoId character varying(64)

	CONSTRAINT uk_eg_land_Address UNIQUE (id),
	CONSTRAINT fk_eg_land_Address FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)
);

CREATE TABLE eg_land_GeoLocation(

	id character varying(64),
	latitude double precision,
	longitude double precision,
	addressId character varying(64),
	additionalDetails JSONB,

	CONSTRAINT fk_eg_bpa_GeoLocation FOREIGN KEY (addressId) REFERENCES eg_land_Address (id)
);

CREATE TABLE eg_land_boundary(

	id character varying(64),
	name character varying(256),
	label character varying(256),
	latitude character varying(256),
	longitude character varying(256),
	addressId character varying(64),
	additionalDetails JSONB,

	CONSTRAINT fk_eg_land_boundary FOREIGN KEY (addressId) REFERENCES eg_land_Address (id)
);

CREATE TABLE eg_land_ownerInfo(
	id character varying(64),
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
	
	landInfoId character varying(64),
	CONSTRAINT uk_eg_land_ownerInfo UNIQUE (id),
	CONSTRAINT pk_eg_land_ownerInfo PRIMARY KEY (id),
	CONSTRAINT fk_eg_land_ownerInfo FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)
);


CREATE TABLE eg_land_institution(
	id character varying(64),
	tenantId character varying(256),
	type character varying(64),
	designation character varying(256),
	nameOfAuthorizedPerson character varying(256),
	additionalDetails JSONB,
	
	landInfoId character varying(64),

	CONSTRAINT uk_eg_land_institution UNIQUE (id),
	CONSTRAINT pk_eg_land_institution PRIMARY KEY (id),
	CONSTRAINT fk_eg_land_institution FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)
);


CREATE TABLE eg_land_document(
	id character varying(64),
	documentType character varying(256),
	fileStore character varying(256),
	documentUid character varying(256),
	additionalDetails JSONB,
	
	landInfoId character varying(64),

	CONSTRAINT uk_eg_land_document UNIQUE (id),
	CONSTRAINT pk_eg_land_document PRIMARY KEY (id),
	CONSTRAINT fk_eg_land_document FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)
);

CREATE TABLE eg_land_unit(

	id character varying(64),
	tenantId character varying(256),
	floorNo character varying(64),	
	unitType character varying(256),
	usageCategory character varying(64),
	occupancyType character varying(64),
	occupancyDate bigint,
	additionalDetails JSONB,
	
	landInfoId character varying(64),
	
	
	CONSTRAINT pk_eg_land_unit PRIMARY KEY (id),
	CONSTRAINT uk_eg_land_unit UNIQUE (id,landInfoId,blockindex),
	CONSTRAINT fk_eg_land_unit FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)

);

CREATE TABLE eg_land_constructionDetail(

	id character varying(64),
	carpetArea bigint,
	builtUpArea bigint,	
	plinthArea bigint,
	superBuiltUpArea bigint,	
	constructionType character varying(64),
	constructionDate bigint,
	dimensions character varying(256),
	additionalDetails JSONB,
	
	landInfoId character varying(64),
	createdBy character varying(64),
	lastModifiedBy character varying(64),
	createdTime bigint,
	lastModifiedTime bigint,
	
	
	CONSTRAINT pk_eg_land_unit PRIMARY KEY (id),
	CONSTRAINT uk_eg_land_unit UNIQUE (id,landInfoId,blockindex),
	CONSTRAINT fk_eg_land_unit FOREIGN KEY (landInfoId) REFERENCES eg_land_landInfo (id)

);

CREATE TABLE public.eg_land_auditdetails
(
    id character varying(256) COLLATE pg_catalog."default" NOT NULL,
    landUid character varying(64) COLLATE pg_catalog."default",
    landUniqueRegNo character varying(256) COLLATE pg_catalog."default",
    tenantId character varying(64) COLLATE pg_catalog."default",
    status character varying(64) COLLATE pg_catalog."default",
    ownershipcategory character varying(64) COLLATE pg_catalog."default",
    source character varying(64) COLLATE pg_catalog."default",
    channel character varying(64) COLLATE pg_catalog."default",
    additionaldetails jsonb,
    createdby character varying(64) COLLATE pg_catalog."default",
    lastmodifiedby character varying(64) COLLATE pg_catalog."default",
    createdtime bigint,
    lastmodifiedtime bigint
);
