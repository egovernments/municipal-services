ALTER TABLE connection
ADD createdBy character varying(64),
ADD lastModifiedBy character varying(64),
ADD createdTime bigint,
ADD lastModifiedTime bigint;