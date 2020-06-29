CREATE TABLE eg_ws_owner (
  tenantId            CHARACTER VARYING (256) NOT NULL,
  connection_id          CHARACTER VARYING (128) NOT NULL,
  userid              CHARACTER VARYING (128) NOT NULL,
  status              CHARACTER VARYING (128) NOT NULL,
  isprimaryowner      BOOLEAN  NOT NULL,
  ownertype           CHARACTER VARYING (256) NOT NULL,
  ownershippercentage CHARACTER VARYING (128),
  institutionId       CHARACTER VARYING (128),
  relationship        CHARACTER VARYING (128),
  createdby           CHARACTER VARYING (128) NOT NULL,
  createdtime         BIGINT NOT NULL,
  lastmodifiedby      CHARACTER VARYING (128),
  lastmodifiedtime    BIGINT,
  CONSTRAINT pk_eg_ws_owner PRIMARY KEY (userid, connection_id),
  CONSTRAINT fk_eg_ws_owner FOREIGN KEY (connection_id) REFERENCES eg_ws_connection (id)
  );