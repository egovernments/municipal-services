CREATE TABLE eg_pt_assessment_v2(

  id character varying(256) NOT NULL,
  tenantId character varying(256) NOT NULL,
  assessmentNumber character varying(64) NOT NULL,
  status character varying(64) NOT NULL,
  propertyId character varying(256) NOT NULL,
  source character varying(64) NOT NULL,
  assessmentDate bigint NOT NULL,
  buildUpArea numeric,
  additionalDetails jsonb,
  createdby character varying(64) NOT NULL,
  createdtime bigint NOT NULL,
  lastmodifiedby character varying(64) NOT NULL,
  lastmodifiedtime bigint NOT NULL,

  CONSTRAINT pk_eg_pt_assessment_v2 PRIMARY KEY (assessmentNumber),
  CONSTRAINT fk_eg_pt_assessment_v2 FOREIGN KEY (propertyId) REFERENCES eg_pt_property_v2 (propertyId)
);



CREATE TABLE eg_pt_assessment_unit_v2 (
  tenantId character varying(256) NOT NULL,
  id character varying(256) NOT NULL,
  assessmentId character varying(256) NOT NULL,
  floorNo	character varying(64),
  unitArea numeric NOT NULL,
  usageCategory character varying(256) NOT NULL,
  occupancyType character varying(64) NOT NULL,
  occupancyDate bigint,
  constructionType character varying(64) NOT NULL,
  arv numeric(12,2),
  createdby character varying(64) NOT NULL,
  createdtime bigint NOT NULL,
  lastmodifiedby character varying(64) NOT NULL,
  lastmodifiedtime bigint NOT NULL,

  CONSTRAINT pk_eg_pt_assessment_unit_v2 PRIMARY KEY (id),
  CONSTRAINT fk_eg_pt_assessment_unit_v2 FOREIGN KEY (assessmentId) REFERENCES eg_pt_assessment_v2 (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE
);