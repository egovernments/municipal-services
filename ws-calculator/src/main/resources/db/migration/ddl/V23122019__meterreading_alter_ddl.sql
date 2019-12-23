ALTER TABLE meterreading
ADD COLUMN consumption decimal;
ALTER TABLE meterreading ALTER COLUMN currentReading TYPE decimal;
ALTER TABLE meterreading ALTER COLUMN lastReading TYPE decimal;
