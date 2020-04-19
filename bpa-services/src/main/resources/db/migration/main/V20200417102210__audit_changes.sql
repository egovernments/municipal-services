DROP TABLE eg_bpa_auditdetails;
CREATE TABLE public.eg_bpa_auditdetails
(
    id character varying(256) COLLATE pg_catalog."default" NOT NULL,
    applicationno character varying(64) COLLATE pg_catalog."default",
    tenantid character varying(256) COLLATE pg_catalog."default",
    edcrnumber character varying(64) COLLATE pg_catalog."default",
    servicetype character varying(256) COLLATE pg_catalog."default" NOT NULL,
    status character varying(64) COLLATE pg_catalog."default",
    additionaldetails jsonb,
    ownershipcategory character varying(64) COLLATE pg_catalog."default",
    createdby character varying(64) COLLATE pg_catalog."default",
    lastmodifiedby character varying(64) COLLATE pg_catalog."default",
    createdtime bigint,
    lastmodifiedtime bigint,
    applicationtype character varying(64) COLLATE pg_catalog."default" NOT NULL,
    risktype character varying(256) COLLATE pg_catalog."default" NOT NULL,
    action character varying(64) COLLATE pg_catalog."default",
    occupancytype character varying(64) DEFAULT NULL,
    suboccupancytype character varying(64) DEFAULT NULL,
    usages character varying(64) DEFAULT NULL,
    permitOrderNo character varying(64) DEFAULT NULL,
    remarks character varying(500) DEFAULT NULL,
    holdingNo character varying(64) DEFAULT NULL,
    registrationDetails character varying(64) DEFAULT NULL,
    govtOrQuasi character varying(64) DEFAULT NULL,    
    validityDate bigint,
    orderGeneratedDate bigint
);