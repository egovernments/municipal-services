CREATE TABLE eg_pt_mutation_billingslab
(
    id character varying(64) NOT NULL,
    tenatid character varying(256) NOT NULL,
    propertyType character varying(64),
    propertySubType character varying(64),
    usuageCategoryMajor character varying(64),
    usuageCategoryMinor character varying(64),
    usuageCategorySubMinor character varying(64),
    usuageCategoryDetail character varying(64),
    ownershipCategory character varying(64),
    subOwnershipCategory character varying(64),
    minMarketValue float,
    maxMarketValue float,
    fixedAmount float
);
