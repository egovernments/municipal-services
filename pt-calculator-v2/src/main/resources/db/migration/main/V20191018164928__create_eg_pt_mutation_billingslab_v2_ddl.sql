CREATE TABLE eg_pt_mutation_billingslab_v2(
id character varying(64),
tenantId character varying(256),
ownerShipType character varying(64),
usageType character varying(64),
areaType character varying(64),
fromCurrentMarketValue numeric(12,2),
toCurrentMarketValue numeric(12,2),
cmvPercent bigint,fixedAmount bigint,
createdby character varying(64),
createdtime bigint,
lastmodifiedby character varying(64),
lastmodifiedtime bigint,

CONSTRAINT pk_eg_pt_billingslab_v2 PRIMARY KEY (id, tenantid),
CONSTRAINT uk_eg_pt_billingslab_v2 UNIQUE (id)
);