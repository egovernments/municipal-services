ALTER TABLE eg_ws_connection
ADD COLUMN IF NOT EXISTS billing_type character varying(64),
ADD COLUMN IF NOT EXISTS billing_amount numeric(12,2),
ADD COLUMN IF NOT EXISTS connection_category character varying(64),
ADD COLUMN IF NOT EXISTS ledger_id character varying(64),
ADD COLUMN IF NOT EXISTS average_make character varying(64),
ADD COLUMN IF NOT EXISTS meter_make character varying(64);

ALTER TABLE eg_ws_connection_audit
ADD COLUMN IF NOT EXISTS billing_type character varying(64),
ADD COLUMN IF NOT EXISTS billing_amount numeric(12,2),
ADD COLUMN IF NOT EXISTS connection_category character varying(64),
ADD COLUMN IF NOT EXISTS ledger_id character varying(64),
ADD COLUMN IF NOT EXISTS average_make character varying(64),
ADD COLUMN IF NOT EXISTS meter_make character varying(64);
