DELETE FROM eg_bpa_blocks WHERE id is null;
ALTER TABLE eg_bpa_blocks ADD CONSTRAINT pk_bpa_blocks PRIMARY KEY(id);