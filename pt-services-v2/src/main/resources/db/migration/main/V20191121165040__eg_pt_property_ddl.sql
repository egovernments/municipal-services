DROP TABLE IF EXISTS eg_pt_property;
DROP TABLE IF EXISTS eg_pt_address;
DROP TABLE IF EXISTS eg_pt_document;
DROP TABLE IF EXISTS eg_pt_owner;
DROP TABLE IF EXISTS eg_pt_institution;

--> Property table

CREATE TABLE eg_pt_property (

   id                   CHARACTER VARYING (128) NOT NULL,
   propertyId           CHARACTER VARYING (128) NOT NULL,
   tenantId             CHARACTER VARYING (256) NOT NULL,
   accountId            CHARACTER VARYING (128) NOT NULL,
   oldPropertyId        CHARACTER VARYING (128),
   status               CHARACTER VARYING (128) NOT NULL,
   acknowldgementNumber CHARACTER VARYING (128) NOT NULL,
   propertyType         CHARACTER VARYING (128) NOT NULL,
   ownershipCategory    CHARACTER VARYING (128) NOT NULL,
   creationReason       CHARACTER VARYING (128),
   occupancyDate        BIGINT,
   constructionDate     BIGINT NOT NULL,
   noOfFloors           BIGINT, 
   landArea             NUMERIC(102) NOT NULL,
   source               CHARACTER VARYING (128) NOT NULL,
   createdBy            CHARACTER VARYING (128) NOT NULL,
   parentproperties     CHARACTER VARYING [],
   lastModifiedBy       CHARACTER VARYING (128),
   createdTime          BIGINT NOT NULL,
   lastModifiedTime     BIGINT,
   additionaldetails    JSONB,

CONSTRAINT pk_eg_pt_property_id PRIMARY KEY(id),
CONSTRAINT uk_eg_pt_property_propertyId UNIQUE (propertyId)
);

CREATE INDEX IF NOT EXISTS index_eg_pt_property_tenantid (tenantid);
CREATE INDEX IF NOT EXISTS index_eg_pt_property_accountId (accountId);
CREATE INDEX IF NOT EXISTS index_eg_pt_property_parentproperties (parentproperties);

--> Institution

CREATE TABLE eg_pt_institution (

  id               CHARACTER VARYING (128) NOT NULL,
  proeprtyid       CHARACTER VARYING (128) NOT NULL,
  tenantId         CHARACTER VARYING (256) NOT NULL,
  name             CHARACTER VARYING (128) NOT NULL,
  type             CHARACTER VARYING (128) NOT NULL,
  designation      CHARACTER VARYING (128),
  createdby        CHARACTER VARYING (128) NOT NULL,
  createdtime      bigint NOT NULL,
  lastmodifiedby   CHARACTER VARYING (128),
  lastmodifiedtime bigint,

  CONSTRAINT pk_eg_pt_institution_v2 PRIMARY KEY (id),
  CONSTRAINT fk_eg_pt_institution_v2 FOREIGN KEY (property) REFERENCES eg_pt_property (propertyid)
);

--> Owner 

CREATE TABLE eg_pt_owner (

  tenantId            CHARACTER VARYING (256) NOT NULL,
  propertyid          CHARACTER VARYING (128) NOT NULL,
  userid              CHARACTER VARYING (128) NOT NULL,
  isactive            BOOLEAN NOT NULL,
  isprimaryowner      BOOLEAN  NOT NULL,
  ownertype           CHARACTER VARYING (64) NOT NULL,
  ownershippercentage CHARACTER VARYING (64),
  institutionId       CHARACTER VARYING (64),
  relationship        CHARACTER VARYING (64),
  createdby           CHARACTER VARYING (64) NOT NULL,
  createdtime         BIGINT NOT NULL,
  lastmodifiedby      CHARACTER VARYING (64),
  lastmodifiedtime    BIGINT,

  CONSTRAINT pk_eg_pt_owner PRIMARY KEY (userid, propertydetail),
  CONSTRAINT fk_eg_pt_owner FOREIGN KEY (propertyid) REFERENCES eg_pt_property (propertyid)
  );

  CREATE INDEX IF NOT EXISTS index_eg_pt_property_tenantid (tenantid);

  --> document table

CREATE TABLE eg_pt_document (

  id               CHARACTER VARYING (128) NOT NULL,
  entityid         CHARACTER VARYING (128) NOT NULL,
  documentType     CHARACTER VARYING (128) NOT NULL,
  fileStore        CHARACTER VARYING (128) NOT NULL,
  documentuid      CHARACTER VARYING (128) NOT NULL,
  createdBy        CHARACTER VARYING (128) NOT NULL,
  lastModifiedBy   CHARACTER VARYING (128),
  createdTime      BIGINT NOT NULL,
  lastModifiedTime BIGINT, 

CONSTRAINT pk_eg_pt_document_id PRIMARY KEY(id),
CONSTRAINT uk_eg_pt_document_documentUid UNIQUE (documentUid),
);

CREATE INDEX IF NOT EXISTS index_eg_pt_property_parentid (entityid);

--> address

CREATE TABLE eg_pt_address (

  tenantId         CHARACTER VARYING(256) NOT NULL,
  id               CHARACTER VARYING(128) NOT NULL,
  propertyid       CHARACTER VARYING(128) NOT NULL,
  latitude         NUMERIC(9,6),
  longitude        NUMERIC(10,7),
  addressid        CHARACTER VARYING(128) NOT NULL,
  addressnumber    CHARACTER VARYING(128),
  doorNo           CHARACTER VARYING(64),
  type             CHARACTER VARYING(64) NOT NULL,
  addressline1     CHARACTER VARYING(1024),
  addressline2     CHARACTER VARYING(1024),
  landmark         CHARACTER VARYING(1024),
  city             CHARACTER VARYING(1024) NOT NULL,
  pincode          CHARACTER VARYING(16) NOT NULL,
  detail           CHARACTER VARYING(2048),
  buildingName     CHARACTER VARYING(1024),
  street           CHARACTER VARYING(1024),
  locality         CHARACTER VARYING(128) NOT NULL,
  createdby        CHARACTER VARYING(128) NOT NULL,
  createdtime      BIGINT NOT NULL,
  lastmodifiedby   CHARACTER VARYING(128),
  lastmodifiedtime BIGINT,

  CONSTRAINT pk_eg_pt_address PRIMARY KEY (id,property),
  CONSTRAINT fk_eg_pt_address FOREIGN KEY (propertyid) REFERENCES eg_pt_property (propertyId)
);

CREATE INDEX IF NOT EXISTS index_eg_pt_address_tenantid (tenantid);