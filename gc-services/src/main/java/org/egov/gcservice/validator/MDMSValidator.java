package org.egov.gcservice.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.gcservice.repository.ServiceRequestRepository;
import org.egov.gcservice.util.GCConstants;
import org.egov.gcservice.util.GarbageServicesUtil;
import org.egov.gcservice.web.models.RoadCuttingInfo;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MDMSValidator {
	@Autowired
	private GarbageServicesUtil garbageServicesUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	public void validateMasterData(GarbageConnectionRequest request, int reqType) {
			switch (reqType) {
				case GCConstants.UPDATE_APPLICATION:
					validateMasterDataForUpdateConnection(request);
					break;
				case GCConstants.MODIFY_CONNECTION:
					validateMasterDataForModifyConnection(request);
					break;
				default:
					break;
	     }
	}


	public Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonPath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		MdmsCriteriaReq criteriaReq = garbageServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		try {
			Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
			return JsonPath.read(result, jsonPath);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException("INVALID_CONNECTION_TYPE", GCConstants.INVALID_CONNECTION_TYPE);
		}
	}

	private void validateMDMSData(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS_DATA_ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private static Map<String, String> validateCodes(GarbageConnection GarbageConnection,
			Map<String, List<String>> codes, Map<String, String> errorMap) {
		StringBuilder messageBuilder;
//		if (GarbageConnection.getConnectionType() != null 
//				&& !codes.get(GCConstants.MDMS_SW_CONNECTION_TYPE).contains(GarbageConnection.getConnectionType())) {
//			messageBuilder = new StringBuilder();
//			messageBuilder.append("GarbageConnection type value is invalid, please enter proper value! ");
//			errorMap.put("INVALID SEWERAGE CONNECTION TYPE", messageBuilder.toString());
//		}
		
//		if(GarbageConnection.getRoadCuttingInfo() == null){
//			errorMap.put("INVALID_ROAD_INFO", "Road Cutting Information should not be empty");
//		}
//
//		if(GarbageConnection.getRoadCuttingInfo() != null){
//			for(RoadCuttingInfo roadCuttingInfo : GarbageConnection.getRoadCuttingInfo()){
//				if (!StringUtils.isEmpty(roadCuttingInfo.getRoadType())
//						&& !codes.get(GCConstants.SC_ROADTYPE_MASTER).contains(roadCuttingInfo.getRoadType())) {
//					messageBuilder = new StringBuilder();
//					messageBuilder.append("Road type value is invalid, please enter proper value! ");
//					errorMap.put("INVALID_WATER_ROAD_TYPE", messageBuilder.toString());
//				}
//			}
//		}
//		
		return errorMap;
	}
	
	/**
	 * Validate master data of sewerage connection request
	 *
	 * @param request sewerage connection request
	 */
	public void validateMasterForCreateRequest(GarbageConnectionRequest request) {
		// calling property related master
		List<String> propertyModuleMasters = new ArrayList<>(Arrays.asList(GCConstants.PROPERTY_OWNERTYPE));
		Map<String, List<String>> codesFromPropetyMasters = getAttributeValues(request.getGarbageConnection().getTenantId(),
				GCConstants.PROPERTY_MASTER_MODULE, propertyModuleMasters, "$.*.code",
				GCConstants.PROPERTY_JSONPATH_ROOT, request.getRequestInfo());
		// merge codes
		String[] finalmasterNames = {GCConstants.PROPERTY_OWNERTYPE};
		validateMDMSData(finalmasterNames, codesFromPropetyMasters);
		validateCodesForCreateRequest(request, codesFromPropetyMasters);
	}

	/**
	 *  @param request Sewerage connection request
	 * @param codes list of master data codes to verify against the sewerage connection request
	 */
	public void validateCodesForCreateRequest(GarbageConnectionRequest request, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		if (!CollectionUtils.isEmpty(request.getGarbageConnection().getConnectionHolders())) {
			request.getGarbageConnection().getConnectionHolders().forEach(holderDetail -> {
				if (!StringUtils.isEmpty(holderDetail.getOwnerType())
						&&
						!codes.get(GCConstants.PROPERTY_OWNERTYPE).contains(holderDetail.getOwnerType())) {
					errorMap.put("INVALID_CONNECTION_HOLDER_TYPE",
							"The GarbageConnection holder type '" + holderDetail.getOwnerType() + "' does not exists");
				}
			});
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	public void validateMasterDataForUpdateConnection(GarbageConnectionRequest request) {
//		if (request.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.ACTIVATE_CONNECTION_CONST)) {
//			Map<String, String> errorMap = new HashMap<>();
//			List<String> names = new ArrayList<>(Arrays.asList(GCConstants.MDMS_SW_CONNECTION_TYPE));
//			List<String> taxModelnames = new ArrayList<>(Arrays.asList(GCConstants.SC_ROADTYPE_MASTER));
//			Map<String, List<String>> codes = getAttributeValues(request.getGarbageConnection().getTenantId(),
//					GCConstants.MDMS_SW_MOD_NAME, names, "$.*.code",
//					GCConstants.JSONPATH_ROOT, request.getRequestInfo());
//			Map<String, List<String>> codeFromCalculatorMaster = getAttributeValues(request.getGarbageConnection().getTenantId(),
//					GCConstants.SW_TAX_MODULE, taxModelnames, "$.*.code",
//					GCConstants.TAX_JSONPATH_ROOT, request.getRequestInfo());
//			// merge codes
//			String[] masterNames = {GCConstants.MDMS_SW_CONNECTION_TYPE, GCConstants.SC_ROADTYPE_MASTER};
//			Map<String, List<String>> finalcodes = Stream.of(codes, codeFromCalculatorMaster).map(Map::entrySet)
//					.flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//			validateMDMSData(masterNames, finalcodes);
//			validateCodes(request.getGarbageConnection(), finalcodes, errorMap);
//			if (!errorMap.isEmpty())
//				throw new CustomException(errorMap);
//		}
	}

	public  void validateMasterDataForModifyConnection(GarbageConnectionRequest request) {
//		if (request.getGarbageConnection().getProcessInstance().getAction().equalsIgnoreCase(GCConstants.APPROVE_CONNECTION)) {
//			Map<String, String> errorMap = new HashMap<>();
//			List<String> names = new ArrayList<>(Arrays.asList(GCConstants.MDMS_SW_CONNECTION_TYPE));
//			List<String> taxModelnames = new ArrayList<>(Arrays.asList(GCConstants.SC_ROADTYPE_MASTER));
//			Map<String, List<String>> codes = getAttributeValues(request.getGarbageConnection().getTenantId(),
//					GCConstants.MDMS_SW_MOD_NAME, names, "$.*.code",
//					GCConstants.JSONPATH_ROOT, request.getRequestInfo());
//			Map<String, List<String>> codeFromCalculatorMaster = getAttributeValues(request.getGarbageConnection().getTenantId(),
//					GCConstants.SW_TAX_MODULE, taxModelnames, "$.*.code",
//					GCConstants.TAX_JSONPATH_ROOT, request.getRequestInfo());
//			// merge codes
//			String[] masterNames = {GCConstants.MDMS_SW_CONNECTION_TYPE, GCConstants.SC_ROADTYPE_MASTER};
//			Map<String, List<String>> finalcodes = Stream.of(codes, codeFromCalculatorMaster).map(Map::entrySet)
//					.flatMap(Collection::stream).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//			validateMDMSData(masterNames, finalcodes);
//			validateCodes(request.getGarbageConnection(), finalcodes, errorMap);
//			if (!errorMap.isEmpty())
//				throw new CustomException(errorMap);
//		}
	}

}
