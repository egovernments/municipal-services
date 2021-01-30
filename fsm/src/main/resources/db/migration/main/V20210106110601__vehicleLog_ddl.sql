
CREATE TABLE IF NOT EXISTS  eg_vehicle_log(
    id character varying(256) NOT NULL,
    applicationno character varying(128),
    tenantid character varying(64),
    status character varying(64),
    applicationstatus character varying(64) NOT NULL,
    dso_id character varying(64) NOT NULL,
    vehicle_id character varying(64) NOT NULL,
    waste_dumped int,
    dump_time bigint,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT pk_eg_vehicle_log_key PRIMARY KEY (id) 
); 

CREATE INDEX  IF NOT EXISTS  index_id_eg_vehicle_log  ON eg_vehicle_log
(    id
);
CREATE INDEX  IF NOT EXISTS  index_dso_eg_vehicle_log  ON eg_vehicle_log
(    dso_id
);
CREATE INDEX  IF NOT EXISTS  index_vehicle_eg_vehicle_log  ON eg_vehicle_log
(    vehicle_id
);
CREATE INDEX  IF NOT EXISTS  index_tenant_eg_vehicle_log  ON eg_vehicle_log
(    tenantid
);

CREATE TABLE IF NOT EXISTS  eg_vehicle_log_auditlog(
    id character varying(256) NOT NULL,
    applicationno character varying(128),
    tenantid character varying(64),
    status character varying(64),
    applicationStatus character varying(64) NOT NULL,
    dso_id character varying(64) NOT NULL,
    vehicle_id character varying(64) NOT NULL,
    waste_dumped int,
    dump_time bigint,
    createdby character varying(64),
    lastmodifiedby character varying(64),
    createdtime bigint,
    lastmodifiedtime bigint,
    CONSTRAINT pk_eg_vehicle_log PRIMARY KEY (id) 
); 

CREATE INDEX  IF NOT EXISTS  index_id_eg_vehicle_log_auditlog  ON eg_vehicle_log_auditlog
(    id
);

CREATE TABLE IF NOT EXISTS eg_vehicle_log_fsm(
vehicle_log_id character varying(64) NOT NULL,
fsm_id character varying(64) NOT NULL,
CONSTRAINT fk_eg_vehicle_log_fsm FOREIGN KEY (vehicle_log_id) REFERENCES eg_vehicle_log (id)
);

CREATE INDEX  IF NOT EXISTS  index_vehiclelogid_eg_vehicle_log_fsm  ON eg_vehicle_log_fsm
(
    vehicle_log_id
);