CREATE TABLE eg_sw_connectionholder (
  tenantId            CHARACTER VARYING (256) NOT NULL,
  connectionid      CHARACTER VARYING (128) NOT NULL,
  status              CHARACTER VARYING (128) NOT NULL,
  userid              CHARACTER VARYING (128),
  isprimaryholder      BOOLEAN  NULL,
  connectionholdertype           CHARACTER VARYING (256) NOT NULL,
  holdershippercentage CHARACTER VARYING (128),
  relationship        CHARACTER VARYING (128),
  createdby           CHARACTER VARYING (128) NOT NULL,
  createdtime         BIGINT NOT NULL,
  lastmodifiedby      CHARACTER VARYING (128),
  lastmodifiedtime    BIGINT,
  CONSTRAINT pk_eg_sw_connectionholder PRIMARY KEY (userid, connectionid),
  CONSTRAINT fk_eg_sw_connectionholder FOREIGN KEY (connectionid) REFERENCES eg_sw_connection (id)
  );