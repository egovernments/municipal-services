ALTER TABLE water_service_connection
ADD detailsprovidedby character varying(256);
ALTER TABLE eg_ws_plumberinfo
DROP column detailsprovidedby;