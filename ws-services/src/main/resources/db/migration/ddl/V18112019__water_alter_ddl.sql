ALTER TABLE connection
ADD COLUMN roadCuttingArea FLOAT,
ADD COLUMN action character varying(64),
ADD COLUMN roadType character varying(32),
DROP COLUMN documents_id;


CREATE TABLE public.eg_ws_plumberinfo
(
  id character varying(256) NOT NULL,
  name character varying(256),
  licenseno character varying(256),
  mobilenumber character varying(256),
  gender character varying(256),
  fatherorhusbandname character varying(256),
  correspondenceaddress character varying(1024),
  relationship character varying(256),
  wsid character varying(64),
  CONSTRAINT uk_eg_ws_plumberinfo PRIMARY KEY (id),
  CONSTRAINT fk_eg_ws_plumberinfo_connection_id FOREIGN KEY (wsid)
      REFERENCES public.connection (id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE public.eg_ws_applicationdocument
(
  id character varying(64) NOT NULL,
  tenantid character varying(64),
  documenttype character varying(64),
  filestoreid character varying(64),
  wsid character varying(64),
  active boolean,
  createdby character varying(64),
  lastmodifiedby character varying(64),
  createdtime bigint,
  lastmodifiedtime bigint,
  CONSTRAINT uk_eg_ws_applicationdocument PRIMARY KEY (id),
  CONSTRAINT fk_eg_ws_applicationdocument_connection_id FOREIGN KEY (wsid)
      REFERENCES public.connection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);