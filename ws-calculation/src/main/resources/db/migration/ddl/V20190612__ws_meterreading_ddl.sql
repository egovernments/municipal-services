DROP TABLE IF EXISTS meterreading;
CREATE TABLE meterreading
(
  connectionId character varying(64) PRIMARY KEY,
  billingPeriod character varying(64) NOT NULL,
  meterStatus character varying(64) NOT NULL,
  lastReading int NOT NULL,
  lastReadingDate bigint NOT NULL,
  currentReading int NOT NULL,
  currentReadingDate bigint NOT NULL,
  consumption int NOT NULL
);