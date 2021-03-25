package org.egov.rb.contract;

import java.util.LinkedList;
import java.util.List;

import org.egov.rb.user.models.ActionInfo;
import org.egov.common.contract.request.RequestInfo;
import org.egov.rb.user.models.Services;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceRequest {
	
	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo=null;
	
	@JsonProperty("actionInfo")
	private List<ActionInfo> actionInfo= new LinkedList<ActionInfo>();
	
	
	 @JsonProperty("services")
	private List<Services> services=new LinkedList<Services>();
	 
	
	 

}
