CREATE TABLE eg_gc_connection (
	id VARCHAR PRIMARY KEY,
	tenantid VARCHAR ( 50 ) NOT NULL,
	property_id VARCHAR ( 50 ) NOT NULL,
	applicationno VARCHAR ( 255 ) NOT NULL,
	status VARCHAR ( 50 )  NOT NULL,
	connectionno VARCHAR ( 50 ) NOT NULL,
	oldconnectionno VARCHAR ( 255 )  NOT NULL,
	action VARCHAR ( 50 )  NOT NULL,
	plotsize NUMERIC ( 50 ) NOT NULL,
	street VARCHAR ( 255 )  NOT NULL,
	pincode NUMERIC NOT NULL,
    buildingname VARCHAR ( 255 ) ,
	doorno VARCHAR ( 50 ) NOT NULL,
	propertyType VARCHAR ( 50 ) NOT NULL,
	usagetype VARCHAR ( 255 ) NOT NULL,
	occupancy VARCHAR ( 50 )  NOT NULL,
	connectioncategory VARCHAR ( 50 ) NOT NULL,
	adhocrebate NUMERIC ( 255 )  NOT NULL,
	adhocpenalty NUMERIC ( 50 )  NOT NULL,
	adhocpenaltyreason VARCHAR ( 50 ) NOT NULL,
	adhocpenaltycomment VARCHAR ( 255 )  NOT NULL,
	adhocrebatereason VARCHAR NOT NULL,
	adhocrebatecomment VARCHAR ( 50 ) NOT NULL,
	createdby VARCHAR ( 50 ) NOT NULL,
	lastmodifiedby VARCHAR ( 255 ) NOT NULL,
	createdtime BIGINT NOT NULL,
	lastmodifiedtime BIGINT NOT NULL,
	applicationtype VARCHAR ( 255 )  NOT NULL,
	effectivefrom BIGINT  NOT NULL,
	locality VARCHAR ( 50 ) NOT NULL,
	islegacy VARCHAR ( 255 )  NOT NULL,
	familymembers VARCHAR ( 50 ),
	additionaldetails VARCHAR (255) 
);


CREATE INDEX IF NOT EXISTS index_eg_gc_connection_tenantid ON eg_gc_connection (tenantid);
CREATE INDEX IF NOT EXISTS index_eg_gc_connection_applicationNo ON eg_gc_connection (applicationno);
CREATE INDEX IF NOT EXISTS index_eg_gc_connection_connectionNo ON eg_gc_connection (connectionno);
CREATE INDEX IF NOT EXISTS index_eg_gc_connection_oldConnectionNo ON eg_gc_connection (oldconnectionno);
CREATE INDEX IF NOT EXISTS index_eg_gc_connection_property_id ON eg_gc_connection (property_id);
CREATE INDEX IF NOT EXISTS index_eg_gc_connection_applicationstatus ON eg_gc_connection (status);