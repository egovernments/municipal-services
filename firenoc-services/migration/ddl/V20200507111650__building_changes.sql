ALTER TABLE eg_fn_buidlings
    ADD COLUMN leftsurrounding character varying(256) ;
ALTER TABLE eg_fn_buidlings
    ADD COLUMN rightsurrounding character varying(256) ;
ALTER TABLE eg_fn_buidlings
    ADD COLUMN frontsurrounding character varying(256) ; 
ALTER TABLE eg_fn_buidlings
    ADD COLUMN backsurrounding character varying(256) ;

ALTER TABLE eg_fn_buidlings
    ADD COLUMN landarea numeric	 not null  DEFAULT 0.0 ;   
 
ALTER TABLE eg_fn_buidlings
    ADD COLUMN totalcoveredarea numeric	 not null  DEFAULT 0.0 ;

ALTER TABLE eg_fn_buidlings
    ADD COLUMN parkingarea numeric DEFAULT 0.0 ;        
