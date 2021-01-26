package org.egov.vehicle.config;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class VehicleConfiguration {

    // Persister Config
    @Value("${persister.save.vehicle.topic}")
    private String saveTopic;

	@Value("${egov.hrms.host}")
	private String employeeHost;

	@Value("${egov.hrms.context.path}")    
	private String employeeContextPath;
	@Value("${egov.hrms.create.path}")
	private String employeeCreateEndpoint;

	@Value("${egov.hrms.search.path}")
	private String employeeSearchEndpoint;

	@Value("${egov.hrms.update.path}")
	private String employeeUpdateEndpoint;
	
	// User Configuration
		@Value("${egov.user.host}")
		private String userHost;

		@Value("${egov.user.context.path}")    
		private String userContextPath;
		
		
		@Value("${egov.user.create.path}")
		private String userCreateEndpoint;

		@Value("${egov.user.search.path}")
		private String userSearchEndpoint;

		@Value("${egov.user.update.path}")
		private String userUpdateEndpoint;
		
		@Value("${egov.user.username.prefix}")
		private String usernamePrefix;
		
		// MDMS
		@Value("${egov.mdms.host}")
		private String mdmsHost;

		@Value("${egov.mdms.search.endpoint}")
		private String mdmsEndPoint;
		
		@Value("${citizen.allowed.search.params}")
		private String allowedCitizenSearchParameters;

		@Value("${employee.allowed.search.params}")
		private String allowedEmployeeSearchParameters;
		
		@Value("${egov.vehicle.default.limit}")
		private Integer defaultLimit;

		@Value("${egov.vehicle.default.offset}")
		private Integer defaultOffset;

		@Value("${egov.vehicle.max.limit}")
		private Integer maxSearchLimit;
	
}
