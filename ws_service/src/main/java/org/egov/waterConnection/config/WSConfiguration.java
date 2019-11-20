package org.egov.waterConnection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Component
public class WSConfiguration {

	@Value("${egov.waterservice.pagination.default.limit}")
	private Integer defaultLimit;

	@Value("${egov.waterservice.pagination.default.offset}")
	private Integer defaultOffset;
	
	 //IDGEN
    @Value("${egov.idgen.wcid.name}")
    private String waterConnectionIdGenName;

    @Value("${egov.idgen.wcid.format}")
    private String waterConnectionIdGenFormat;
    
    @Value("${egov.idgen.scid.name}")
    private String sewerageIdGenName;

    @Value("${egov.idgen.scid.format}")
    private String sewerageIdGenFormat;
    
    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

}
