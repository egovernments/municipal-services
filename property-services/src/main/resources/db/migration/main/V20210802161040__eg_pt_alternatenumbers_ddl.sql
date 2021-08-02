DROP TABLE IF EXISTS eg_pt_alternatenumbers;

--> Alternate number table

CREATE TABLE eg_pt_alternatenumbers(

   id                   CHARACTER VARYING (128) NOT NULL,
   propertyid           CHARACTER VARYING (256),
   tenantid             CHARACTER VARYING (256) NOT NULL,
   name					CHARACTER VARYING (256) NOT NULL,
   mobilenumber			CHARACTER VARYING (256) NOT NULL,
   
   CONSTRAINT fk_eg_pt_alternatenumbers FOREIGN KEY (id,propertyid,tenantid) REFERENCES eg_pt_property (id,propertyid,tenantid)

);