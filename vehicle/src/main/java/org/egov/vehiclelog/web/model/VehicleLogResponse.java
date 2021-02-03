package org.egov.vehiclelog.web.model;

import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * Response of Vendor detail
 */
//@Schema(description = "Response of Vendor detail")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:34:12.238Z[GMT]")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class VehicleLogResponse {

	@JsonProperty("responseInfo")
	private ResponseInfo responseInfo = null;
	
	 @JsonProperty("vendor")
	 private List<VehicleLog> vehicleLog = null;
	 
	 

}
