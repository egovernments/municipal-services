DROP TABLE IF EXISTS sewarage_service_connection;
DROP TABLE IF EXISTS connection;

DROP TABLE IF EXISTS eg_sw_service;
DROP TABLE IF EXISTS eg_sw_connection;


CREATE TABLE eg_sw_connection
(
  id character varying(64) NOT NULL,
  property_id character varying(64) NOT NULL,
  applicationno character varying(64),
  applicationstatus character varying(256),
  status character varying(64) NOT NULL,
  connectionno character varying(256) NOT NULL,
  oldconnectionno character varying(64),
  documents_id character varying(256),
  CONSTRAINT connection_pkey PRIMARY KEY (id)
);



CREATE TABLE eg_sw_service
(
  connection_id character varying(64) NOT NULL,
  connectionExecutionDate bigint NOT NULL,
  noOfWaterClosets integer,
  noOfToilets integer,
  UOM character varying(32),
  connectiontype character varying(32) NOT NULL,
  calculationAttribute character varying(64),
  CONSTRAINT sewarage_service_connection_connection_id_fkey FOREIGN KEY (connection_id)
  REFERENCES connection (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE
);