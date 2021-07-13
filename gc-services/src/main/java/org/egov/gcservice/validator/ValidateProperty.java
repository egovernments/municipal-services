package org.egov.gcservice.validator;

import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.web.models.Property;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.gcservice.web.models.Status;
import org.egov.tracer.model.CustomException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import static org.egov.gcservice.util.GCConstants.PROPERTY_JSONPATH_ROOT;
import static org.egov.gcservice.util.GCConstants.PROPERTY_MASTER_MODULE;

@Component
@Slf4j
public class ValidateProperty {

	@Autowired
	private GarbageServicesUtil sewerageServiceUtil;

	@Autowired
	private MDMSValidator mdmsValidator;

	/**
	 * 
	 * @param property
	 *            property
	 */
	public void validatePropertyFields(Property property,RequestInfo requestInfo) {
		if (StringUtils.isEmpty(property.getTenantId())) {
			throw new CustomException("INVALID_PROPERTY", "GarbageConnection cannot be updated without tenantId");
		}

		JSONObject mdmsResponse=getWnsPTworkflowConfig(requestInfo,property.getTenantId());
		List<Status> allowedPropertyStatus = new ArrayList<>();
		allowedPropertyStatus.add(Status.ACTIVE);
		if (mdmsResponse.getBoolean("inWorkflowStatusAllowed"))
			allowedPropertyStatus.add(Status.INWORKFLOW);

		if (org.springframework.util.StringUtils.isEmpty(property.getStatus()) || !(allowedPropertyStatus.contains(property.getStatus()))) {
			throw new CustomException("INVALID_PROPERTY_STATUS", " Please enter a valid property ID");
		}
	}
	
	public Property getOrValidateProperty(GarbageConnectionRequest garbageConnectionRequest) {
		Optional<Property> propertyList = sewerageServiceUtil.propertySearch(garbageConnectionRequest).stream()
				.findFirst();
		if (!propertyList.isPresent()) {
			throw new CustomException("INVALID_PROPERTY_ID",
					"Water connection cannot be enriched without property");
		} 
		Property property = propertyList.get();
		if (StringUtils.isEmpty(property.getUsageCategory()) && !(garbageConnectionRequest.getRequestInfo().getUserInfo().getType().equalsIgnoreCase("SYSTEM"))) {
			throw new CustomException("INVALID_PROPERTY_USAGE_TYPE",
					"Water connection cannot be enriched without property usage type");
		}

		return property;
	}

	public void validatePropertyForConnection(List<GarbageConnection> GarbageConnectionList) {
		GarbageConnectionList.forEach(GarbageConnection -> {
			if (StringUtils.isEmpty(GarbageConnection.getPropertyId())) {
				StringBuilder builder = new StringBuilder();
				builder.append("Property not found for Id")
						.append(GarbageConnection.getConnectionNo() == null ? GarbageConnection.getApplicationNo()
								: GarbageConnection.getConnectionNo());
				log.error("INVALID_PROPERTY_ID", builder.toString());
			}
		});
	}
	public JSONObject getWnsPTworkflowConfig(RequestInfo requestInfo, String tenantId){
		tenantId = tenantId.split("\\.")[0];
		List<String> propertyModuleMasters = new ArrayList<>(Arrays.asList("PTWorkflow"));
		Map<String, List<String>> codes = mdmsValidator.getAttributeValues(tenantId,PROPERTY_MASTER_MODULE, propertyModuleMasters, "$.*",
				PROPERTY_JSONPATH_ROOT,requestInfo);
		JSONObject obj = new JSONObject(codes);
		JSONArray configArray = obj.getJSONArray("PTWorkflow");
		JSONObject response = new JSONObject();
		for(int i=0;i<configArray.length();i++){
			if(configArray.getJSONObject(i).getBoolean("enable"))
				response=configArray.getJSONObject(i);
		}
		return response;
	}
}
