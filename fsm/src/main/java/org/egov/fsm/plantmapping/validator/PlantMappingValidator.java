package org.egov.fsm.plantmapping.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.fsm.plantmapping.config.PlantMappingConfiguration;
import org.egov.fsm.plantmapping.service.PlantMappingUserService;
import org.egov.fsm.plantmapping.util.PlantMappingConstants;
import org.egov.fsm.plantmapping.web.model.PlantMapping;
import org.egov.fsm.plantmapping.web.model.PlantMappingRequest;
import org.egov.fsm.plantmapping.web.model.PlantMappingSearchCriteria;
import org.egov.fsm.plantmapping.web.model.UserDetailResponse;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PlantMappingValidator {

	@Autowired
	private MDMSPlantMapValidator mdmsValidator;
	
	@Autowired
	private PlantMappingUserService userService;
	
	@Autowired
	private PlantMappingConfiguration config;
	
	public void validateCreateOrUpdate(@Valid PlantMappingRequest request, Object mdmsData) {
		
		if (StringUtils.isEmpty(request.getPlantMapping().getTenantId())) {
			throw new CustomException(PlantMappingConstants.INVALID_TENANT, "TenantId is mandatory");
		}
		if (request.getPlantMapping().getTenantId().split("\\.").length == 1) {
			throw new CustomException(PlantMappingConstants.INVALID_TENANT, "Invalid TenantId");
		}
		if (request.getPlantMapping().getEmployeeUuid() == null || request.getPlantMapping().getEmployeeUuid().isEmpty()) {
			throw new CustomException(PlantMappingConstants.INVALID_UUID, "At lease one employee uuid is required");
		}
		if (request.getPlantMapping().getPlantCode() == null || request.getPlantMapping().getPlantCode().isEmpty()) {
			throw new CustomException(PlantMappingConstants.INVALID_PLANT_CODE, "");
		}
		mdmsValidator.validateMdmsData(request, mdmsData);
		
		PlantMapping plantMap = request.getPlantMapping();
		plantMap.getEmployeeUuid();
		 if(!request.getRequestInfo().getUserInfo().getType().equalsIgnoreCase(FSMConstants.EMPLOYEE)) {
			 throw new CustomException(FSMErrorConstants.INVALID_APPLICANT_ERROR,"Applicant must be an Employee");
		 }
		 mdmsValidator.validateFSTPPlantInfo(plantMap.getPlantCode());
		 UserDetailResponse userResponse = userService.userExists(request);
		 ArrayList<String> code = new ArrayList<String>();
			if (userResponse.getUser().size() > 0) {
				userResponse.getUser().get(0).getRoles().forEach(role -> {
					code.add("" + role.getCode());
				});
				if (!code.contains(PlantMappingConstants.FSTPO_EMPLOYEE)) {
					throw new CustomException(FSMErrorConstants.INVALID_APPLICANT_ERROR,
							"Only FSTPO Empoyee Can do this creation.");
				}
			}
	}

	public void validateSearch(@Valid PlantMappingSearchCriteria criteria, RequestInfo requestInfo) {
		if(StringUtils.isEmpty(criteria.getTenantId())) {
			throw new CustomException(PlantMappingConstants.INVALID_SEARCH, "TenantId is mandatory in search");
		}
		String allowedParamStr = config.getAllowedPlantMappingSearchParameters();
		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(PlantMappingConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
			validateSearchParams(criteria, allowedParams);
		}
		
	}

	private void validateSearchParams(@Valid PlantMappingSearchCriteria criteria, List<String> allowedParams) {

		if (criteria.getPlantCode() != null && !allowedParams.contains("plantCode"))
			throw new CustomException(PlantMappingConstants.INVALID_SEARCH, "Search on plant code is not allowed");

		if (criteria.getEmployeeUuid() != null && !allowedParams.contains("employeeUuid"))
			throw new CustomException(PlantMappingConstants.INVALID_SEARCH, "Search on  employee uuid is not allowed");
		
		if (criteria.getTenantId() != null && !allowedParams.contains("tenantId"))
			throw new CustomException(PlantMappingConstants.INVALID_SEARCH, "Search on tenantid is not allowed");
		
		
	}


}
