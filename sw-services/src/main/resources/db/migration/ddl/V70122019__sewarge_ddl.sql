CREATE TABLE eg_sw_connection
(
id character varying(64) NOT NULL,
property_id character varying(64) NOT NULL,
tenantid character varying(250) NOT NULL,
applicationno character varying(64),
applicationstatus character varying(256),
status character varying(64) NOT NULL,
connectionno character varying(256) NOT NULL,
oldconnectionno character varying(64),
documents_id character varying(256),
CONSTRAINT eg_sw_connection_pkey PRIMARY KEY (id)
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
CONSTRAINT eg_sw_service_connection_id_fkey FOREIGN KEY (connection_id)
REFERENCES connection (id)
ON UPDATE CASCADE
ON DELETE CASCADE
);