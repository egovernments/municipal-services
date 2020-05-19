ALTER TABLE IF EXISTS eg_bpa_buildingplan RENAME TO eg_bpa_buildingplan_temp;
ALTER TABLE IF EXISTS eg_bpa_buildingplan_temp RENAME CONSTRAINT pk_eg_bpa_buildingplan TO pk_eg_bpa_buildingplan_temp;
ALTER TABLE IF EXISTS eg_bpa_auditdetails RENAME TO eg_bpa_auditdetails_temp;
ALTER TABLE IF EXISTS eg_bpa_document RENAME TO eg_bpa_document_temp;
ALTER TABLE IF EXISTS eg_bpa_document_temp RENAME CONSTRAINT uk_eg_bpa_document TO uk_eg_bpa_document_temp;
ALTER TABLE IF EXISTS eg_bpa_document_temp RENAME CONSTRAINT fk_eg_bpa_document TO fk_eg_bpa_document_temp;