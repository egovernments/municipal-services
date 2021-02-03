package org.egov.vehiclelog.config;

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
public class VehicleLogConfiguration {
	
	// Idgen Config
	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;

	@Value("${egov.idgen.fsm.applicationNum.name}")
	private String applicationNoIdgenName;

	@Value("${egov.idgen.fsm.applicationNum.format}")
	private String applicationNoIdgenFormat;


	@Value("${persister.save.vehicle.log.topic}")
	private String saveVehicleLogTopic;

	@Value("${persister.update.vehicle.log.topic}")
	private String updateVehicleLogTopic;

	// Allowed Search Parameters
	@Value("${vehicle.log.allowed.search.params}")
	private String allowedVehicleLogSearchParameters;
	
	//Pagination params
	@Value("${egov.vehiclelog.default.limit}")
	private Integer defaultLimit;

	@Value("${egov.vehiclelog.default.offset}")
	private Integer defaultOffset;

	@Value("${egov.vehiclelog.max.limit}")
	private Integer maxSearchLimit;

}
