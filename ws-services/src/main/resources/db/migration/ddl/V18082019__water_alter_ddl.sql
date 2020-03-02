ALTER TABLE water_service_connection
DROP column uom;
ALTER TABLE connection
ADD adhocrebate numeric(12,2),
ADD adhocpenalty numeric(12,2);