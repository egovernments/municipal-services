ALTER TABLE eg_sw_connection
ADD COLUMN roadCuttingArea FLOAT,
ADD COLUMN action character varying(64),
ADD COLUMN roadType character varying(32),
DROP COLUMN documents_id;
ALTER TABLE eg_sw_connection alter column connectionno drop not null;
CREATE TABLE public.eg_sw_plumberinfo
(
  id character varying(256) NOT NULL,
  name character varying(256),
  licenseno character varying(256),
  mobilenumber character varying(256),
  gender character varying(256),
  fatherorhusbandname character varying(256),
  correspondenceaddress character varying(1024),
  relationship character varying(256),
  swid character varying(64),
  CONSTRAINT uk_eg_sw_plumberinfo PRIMARY KEY (id),
  CONSTRAINT fk_eg_sw_plumberinfo_eg_sw_connection_id FOREIGN KEY (swid)
      REFERENCES public.eg_sw_connection (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE public.eg_sw_applicationdocument
(
  id character varying(64) NOT NULL,
  tenantid character varying(64),
  documenttype character varying(64),
  filestoreid character varying(64),
  swid character varying(64),
  active character varying(64),
  documentUid character varying(64),
  createdby character varying(64),
  lastmodifiedby character varying(64),
  createdtime bigint,
  lastmodifiedtime bigint,
  CONSTRAINT uk_eg_sw_applicationdocument PRIMARY KEY (id),
  CONSTRAINT fk_eg_sw_applicationdocument_eg_sw_connection_id FOREIGN KEY (swid)
      REFERENCES public.eg_sw_connection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);