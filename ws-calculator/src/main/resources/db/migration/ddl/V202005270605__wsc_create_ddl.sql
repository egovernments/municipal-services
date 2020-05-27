DROP TABLE IF EXISTS meterreading;
CREATE TABLE eg_ws_meterreading
(
  id character varying(64),
  connectionNo character varying(64),
  billingPeriod character varying(64) NOT NULL,
  meterStatus character varying(64) NOT NULL,
  lastReading decimal NOT NULL,
  lastReadingDate bigint NOT NULL,
  currentReading decimal NOT NULL,
  currentReadingDate bigint NOT NULL,
  consumption decimal,
  CONSTRAINT uk_eg_ws_meterreading UNIQUE (id)
);