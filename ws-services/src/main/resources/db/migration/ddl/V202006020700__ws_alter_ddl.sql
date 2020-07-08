CREATE TABLE IF NOT EXISTS eg_ws_connection_audit
(
  id character varying(64) NOT NULL,
  tenantid character varying(250) NOT NULL,
  property_id character varying(64) NOT NULL,
  applicationno character varying(64),
  applicationstatus character varying(256),
  status character varying(64) NOT NULL,
  connectionno character varying(256),
  oldconnectionno character varying(64),
  roadCuttingArea FLOAT,
  action character varying(64),
  roadType character varying(32),
  adhocrebate numeric(12,2),
  adhocpenalty numeric(12,2),
  adhocpenaltyreason character varying(1024),
  adhocpenaltycomment character varying(1024),
  adhocrebatereason character varying(1024),
  adhocrebatecomment character varying(1024),
  createdBy character varying(64),
  lastModifiedBy character varying(64),
  createdTime bigint,
  lastModifiedTime bigint
);

CREATE INDEX IF NOT EXISTS index_eg_ws_connection_tenantId ON eg_ws_connection (tenantid);
CREATE INDEX IF NOT EXISTS index_eg_ws_connection_applicationNo ON eg_ws_connection (applicationno);
CREATE INDEX IF NOT EXISTS index_eg_ws_connection_connectionNo ON eg_ws_connection (connectionno);
CREATE INDEX IF NOT EXISTS index_eg_ws_connection_oldConnectionNo ON eg_ws_connection (oldconnectionno);
CREATE INDEX IF NOT EXISTS index_eg_ws_connection_property_id ON eg_ws_connection (property_id);
CREATE INDEX IF NOT EXISTS index_eg_ws_connection_applicationstatus ON eg_ws_connection (applicationstatus);

CREATE TABLE IF NOT EXISTS eg_ws_service_audit
(
  connection_id character varying(64) NOT NULL,
  connectioncategory character varying(32),
  rainwaterharvesting boolean,
  connectiontype character varying(32),
  watersource character varying(64),
  meterid character varying(64),
  meterinstallationdate bigint,
  pipeSize decimal,
  noOfTaps integer,
  connectionexecutiondate bigint,
  proposedpipesize decimal,
  proposedTaps integer,
  initialmeterreading  numeric(12,3),
  appCreatedDate bigint,
  detailsprovidedby character varying(256),
  estimationfileStoreId character varying(256),
  sanctionfileStoreId character varying(256),
  createdBy character varying(64),
  lastModifiedBy character varying(64),
  createdTime bigint,
  lastModifiedTime bigint,
  estimationLetterDate bigint
);

CREATE INDEX IF NOT EXISTS index_eg_ws_service_appCreatedDate ON eg_ws_service (appCreatedDate);