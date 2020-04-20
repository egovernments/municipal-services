ALTER TABLE eg_bpa_owner
DROP CONSTRAINT pk_eg_bpa_owner;

ALTER TABLE eg_bpa_owner
DROP COLUMN id;

ALTER TABLE eg_bpa_owner
RENAME COLUMN uuid TO id;

ALTER TABLE eg_bpa_owner ADD PRIMARY KEY (id,buildingplanid);


CREATE TABLE eg_bpa_document_owner (
tenantId character varying(256),
id character varying(64),
buildingplanid character varying(64),
owner character varying(128),
documenttype character varying(64),
filestore character varying(64),
active boolean,
documentuid character varying(64),
createdby character varying(64),
createdtime bigint,
lastmodifiedby character varying(64),
lastmodifiedtime bigint,

CONSTRAINT uk_eg_bpa_document_owner PRIMARY KEY (id),
CONSTRAINT pk_eg_bpa_document_owner UNIQUE (owner, buildingplanid),
CONSTRAINT fk_eg_bpa_document_owner FOREIGN KEY (owner, buildingplanid) REFERENCES eg_bpa_owner (id, buildingplanid)
ON UPDATE CASCADE
ON DELETE CASCADE
);

ALTER TABLE eg_bpa_owner ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;