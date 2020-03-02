ALTER TABLE eg_sw_service
DROP column uom;
ALTER TABLE eg_sw_connection
ADD adhocrebate numeric(12,2),
ADD adhocpenalty numeric(12,2);
