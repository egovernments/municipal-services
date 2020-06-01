CREATE TABLE eg_sw_connection_audit
(
	id character varying(64) NOT NULL,
	property_id character varying(64) NOT NULL,
	tenantid character varying(250) NOT NULL,
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

CREATE TABLE eg_sw_service_audit
(
	connection_id character varying(64) NOT NULL,
	connectionExecutionDate bigint,
	noOfWaterClosets integer,
	noOfToilets integer,
	connectiontype character varying(32),
	proposedWaterClosets integer,
	proposedToilets integer,
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