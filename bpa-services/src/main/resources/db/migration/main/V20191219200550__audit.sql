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
    CONSTRAINT pk_eg_bpa_auditdetails PRIMARY KEY (id)
)