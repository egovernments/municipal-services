DROP TABLE IF EXISTS eg_ws_service;
DROP TABLE IF EXISTS eg_ws_connection;

CREATE TABLE eg_ws_connection
(
  id character varying(64) NOT NULL,
  tenantid character varying(250) NOT NULL,
  property_id character varying(64) NOT NULL,
  applicationno character varying(64),
  applicationstatus character varying(256),
  status character varying(64) NOT NULL,
  connectionno character varying(256) NOT NULL,
  oldconnectionno character varying(64),
  documents_id character varying(256),
  CONSTRAINT connection_pkey PRIMARY KEY (id)
);

CREATE TABLE eg_ws_service
(
  connection_id character varying(64) NOT NULL,
  connectioncategory character varying(32) NOT NULL,
  rainwaterharvesting boolean NOT NULL,
  connectiontype character varying(32) NOT NULL,
  watersource character varying(64) NOT NULL,
  meterid character varying(64),
  meterinstallationdate bigint,
  pipeSize decimal,
  noOfTaps integer,
  UOM character varying(32),
  waterSubSource character varying(64),
  calculationAttribute character varying(64),
  CONSTRAINT water_service_connection_connection_id_fkey FOREIGN KEY (connection_id) REFERENCES connection (id) 
  ON UPDATE CASCADE
  ON DELETE CASCADE
);


