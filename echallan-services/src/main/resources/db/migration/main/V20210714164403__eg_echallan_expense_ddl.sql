ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS vendor character varying(256);
ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS typeOfExpense character varying(64);
ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS billDate bigint;
ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS billIssuedDate bigint;
ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS paidDate bigint;
ALTER TABLE eg_echallan ADD COLUMN IF NOT EXISTS isBillPaid boolean;