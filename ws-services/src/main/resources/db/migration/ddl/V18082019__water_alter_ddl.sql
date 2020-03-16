ALTER TABLE water_service_connection
DROP column uom;
ALTER TABLE connection
ADD adhocrebate numeric(12,2),
ADD adhocpenalty numeric(12,2),
ADD adhocpenaltyreason character varying(1024),
ADD adhocpenaltycomment character varying(1024),
ADD adhocrebatereason character varying(1024),
ADD adhocrebatecomment character varying(1024);