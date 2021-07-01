package org.egov.fsm.repository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.RequestInfo.RequestInfoBuilder;
import org.egov.fsm.config.FSMConfiguration;
import org.egov.fsm.repository.querybuilder.DataMartQueryBuilder;
import org.egov.fsm.repository.rowmapper.DataMartRowMapper;
import org.egov.fsm.repository.rowmapper.DataMartTenantRowMapper;
import org.egov.fsm.service.FSMService;
import org.egov.fsm.util.DataMartUtil;
import org.egov.fsm.util.FSMConstants;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.fsm.validator.MDMSValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.sun.tools.sjavac.Log;

import lombok.extern.slf4j.Slf4j;

import org.egov.fsm.web.model.DataMartModel;
import org.egov.fsm.web.model.DataMartTenantModel;
import org.egov.fsm.web.model.RequestInfoWrapper;
import org.egov.fsm.web.model.location.Boundary;
import org.egov.fsm.web.model.workflow.BusinessService;
import org.egov.fsm.web.model.workflow.ProcessInstance;
import org.egov.fsm.web.model.workflow.ProcessInstanceResponse;
import org.egov.fsm.web.model.workflow.State;
import org.egov.fsm.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;

@Repository
@Slf4j
public class DatamartRepository {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	DataMartRowMapper dataMartRowMapper;

	@Autowired
	FSMConfiguration fsmConfiguration;

	@Autowired
	WorkflowService workflowService;

	@Autowired
	ServiceRequestRepository serviceRequestRepository;

	@Autowired
	DataMartTenantRowMapper dataMartTenantRowMapper;

	@Autowired
	DataMartUtil dataMartUtil;

	@Autowired
	MDMSValidator mdmsValidator;

	public List<DataMartModel> getData(RequestInfo requestInfo) {

		String countQuery = DataMartQueryBuilder.countQuery;
		List<DataMartTenantModel> totalrowsWithTenantId = jdbcTemplate.query(countQuery, dataMartTenantRowMapper);

		BusinessService businessService = workflowService.getBusinessService(null, requestInfo,
				FSMConstants.FSM_BusinessService, null);

		StringBuilder query = new StringBuilder(DataMartQueryBuilder.dataMartQuery);
		List<DataMartModel> datamartList = new ArrayList<DataMartModel>();
		for (DataMartTenantModel tenantModel : totalrowsWithTenantId) {
			List<List<LinkedHashMap>> boundaryData = getBoundaryData(tenantModel.getTenantId(), requestInfo);
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = new JSONObject(boundaryData.get(0)).toString();
			List<LinkedHashMap> boundaryObject = boundaryData.get(0);

			Object mdmsData = dataMartUtil.mDMSCall(requestInfo, tenantModel.getTenantId());
			Map<String, List<LinkedHashMap>> masterData = dataMartUtil.groupMdmsDataByMater(mdmsData);
			List<DataMartModel> dataMartList = jdbcTemplate.query(query.toString(), dataMartRowMapper);
			for (DataMartModel dataMartModel : dataMartList) {
				String locality = dataMartModel.getLocality();
				Map<String, ProcessInstance> processInstanceData = getProceessInstanceData(
						dataMartModel.getApplicationId(), requestInfo, tenantModel.getTenantId());
				if(processInstanceData!=null) {
				dataMartModel = enrichWorkFlowData(processInstanceData, dataMartModel, businessService);
				}
				dataMartModel = enrichMasterData(boundaryObject, masterData, dataMartModel);

				datamartList.add(dataMartModel);
			}
			// }
		}
		return datamartList;

	}

	private DataMartModel enrichMasterData(List<LinkedHashMap> boundaryObject,
			Map<String, List<LinkedHashMap>> masterData, DataMartModel dataMartModel) {
		if (dataMartModel.getLocality() != null && boundaryObject != null) {

			List<LinkedHashMap> filteredBoundaryData = boundaryObject.stream()
					.filter(map -> ((String) map.get("code")).equals(dataMartModel.getLocality()))
					.collect(Collectors.toList());
			if (filteredBoundaryData.size() > 0)
				dataMartModel.setLocality(filteredBoundaryData.get(0).get("name").toString());

		}

		if (dataMartModel.getSlumName() != null) {
			List<LinkedHashMap> slumMasterData = masterData.get(FSMConstants.MDMS_SLUM_NAME);
			String slumName = dataMartModel.getSlumName();
			if (slumMasterData != null) {
				List<LinkedHashMap> slumCodeList = slumMasterData.stream()
						.filter(map -> ((String) map.get("code")).equals(slumName)).collect(Collectors.toList());

				if (slumCodeList.size() > 0) {
					dataMartModel.setSlumName(slumCodeList.get(0).get("name").toString());
				}
			}
		}

		if (dataMartModel.getSanitationType() != null) {
			List<LinkedHashMap> sanitationMasterData = masterData.get(FSMConstants.MDMS_SANITATION_TYPE);
			String sanitationType = dataMartModel.getSanitationType();
			List<LinkedHashMap> sanitationCodeList = sanitationMasterData.stream()
					.filter(map -> ((String) map.get("code")).equals(sanitationType)).collect(Collectors.toList());
			if (sanitationCodeList.size() > 0) {
				dataMartModel.setSanitationType(sanitationCodeList.get(0).get("name").toString());
			}
		}

		if (dataMartModel.getApplicationSource() != null) {
			List<LinkedHashMap> applicationMasterData = masterData.get(FSMConstants.MDMS_APPLICATION_CHANNEL);
			String applicationType = dataMartModel.getApplicationSource();
			List<LinkedHashMap> applicationList = applicationMasterData.stream()
					.filter(map -> ((String) map.get("code")).equals(applicationType)).collect(Collectors.toList());
			if (applicationList.size() > 0) {
				dataMartModel.setApplicationSource(applicationList.get(0).get("name").toString());
			}
		}

		if (dataMartModel.getPropertyType() != null) {
			List<LinkedHashMap> propertyTypeMasterData = masterData.get(FSMConstants.MDMS_PROPERTY_TYPE);
			String propertyType = dataMartModel.getPropertyType();
			List<LinkedHashMap> propertyTypeList = propertyTypeMasterData.stream()
					.filter(map -> ((String) map.get("code")).equals(propertyType)).collect(Collectors.toList());
			if (propertyTypeList.size() > 0) {
				dataMartModel.setPropertyType(propertyTypeList.get(0).get("name").toString());
			}
			if (dataMartModel.getPropertySubType() != null) {
				String propertySubType = dataMartModel.getPropertyType().toUpperCase() + "."
						+ dataMartModel.getPropertySubType();
				List<LinkedHashMap> propertySubTypeList = propertyTypeMasterData.stream()
						.filter(map -> ((String) map.get("code")).equals(propertySubType)).collect(Collectors.toList());
				if (propertySubTypeList.size() > 0) {
					dataMartModel.setPropertySubType(propertySubTypeList.get(0).get("name").toString());
				} else {
					if (propertyType.toUpperCase() == propertySubType.toUpperCase()) {
						dataMartModel.setPropertySubType(propertyType);
					}
				}
			}
		}
		return dataMartModel;
	}

	private DataMartModel enrichWorkFlowData(Map<String, ProcessInstance> processInstanceData,
			DataMartModel dataMartModel, BusinessService businessService) {

		for (Map.Entry<String, ProcessInstance> data : processInstanceData.entrySet()) {

			LocalDateTime createdTime = null;

			switch (data.getKey()) {

			case "CREATED": {
				dataMartModel.setCreatedStatus("Application Created");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setCreatedAssignedDateTime(dateTime);
				createdTime = dateTime;
				break;
			}

			case "PENDING_APPL_FEE_PAYMENT": {
				dataMartModel.setPendingForPaymentStatus("Pending for payment");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setPendingForPaymentAssignedTime(dateTime);
				break;
			}

			case "ASSING_DSO": {
				dataMartModel.setAssignDsoStatus("Pending for DSO Assignment");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setAssignDsoAssignedDateTime(dateTime);
				break;
			}

			case "DSO_REJECTED": {
				dataMartModel.setCreatedStatus("DSO Rejected");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setDsoRejectedDateTime(dateTime);
				String comment = processInstance.getComment();
				String[] commentArray = comment.split("~");
				if (commentArray != null && commentArray.length > 0) {
					dataMartModel.setReasonForDecline(commentArray[0]);
				}
				break;
			}

			case "DSO_INPROGRESS": {
				dataMartModel.setDsoInprogressStatus("DSO Inprogress");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setDsoInprogressAssignedTime(dateTime);
				break;
			}
			case "PENDING_DSO_APPROVAL": {
				dataMartModel.setDsoPendingApprovalStatus("Pending for DSO Approval");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setDsoPendingAssignedTime(dateTime);
				break;
			}

			case "COMPLETED": {
				dataMartModel.setComplatedStatus("Completed Request");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setApplicationCompletedTime(dateTime);
				Duration duration = Duration.between(dateTime, createdTime);
				dataMartModel.setSlaDays(duration.toDays());
				dataMartModel.setSlaPlanned((int) (businessService.getBusinessServiceSla() / (1000 * 60 * 60 * 24)));
				break;

			}

			case "REJECTED": {
				dataMartModel.setRejectedStatus("Rejected");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setRejectedDateTime(dateTime);
				String comment = processInstance.getComment();
				String[] commentArray = comment.split("~");
				if (commentArray != null && commentArray.length > 0) {
					dataMartModel.setReasonForRejection(commentArray[0]);
				}

				break;

			}

			case "CANCELED": {
				dataMartModel.setCancelledStatus("Cancelled");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setCancelledDateTime(dateTime);

				break;
			}
			case "CITIZEN_FEEDBACK_PENDING": {
				dataMartModel.setCitizanFeedbackStatus("Citizen feedback pending");
				ProcessInstance processInstance = data.getValue();
				LocalDateTime dateTime = Instant.ofEpochMilli(processInstance.getAuditDetails().getCreatedTime())
						.atZone(ZoneId.systemDefault()).toLocalDateTime();
				dataMartModel.setCitizanFeedbackDateTime(dateTime);
				dataMartModel.setRating(processInstance.getRating());
			}
				break;

			}
		}

		return dataMartModel;
	}

	private Map<String, ProcessInstance> getProceessInstanceData(String applicationId, RequestInfo requestInfo,
			String tenantId) {
		// TODO Auto-generated method stub
		try {
			LinkedHashMap processInstanceResponse = (LinkedHashMap) serviceRequestRepository.fetchResult(
					new StringBuilder(fsmConfiguration.getWfHost() + fsmConfiguration.getWfProcessPath()
							+ "?businessService=FSM&&businessIds=" + applicationId + "&&tenantId=" + tenantId),
					requestInfo);
			List<ProcessInstance> processInstances = (List<ProcessInstance>) processInstanceResponse
					.get("ProcessInstances");
			Map<State, List<ProcessInstance>> processInstanceListMap = processInstances.stream()
					.collect(Collectors.groupingBy(ProcessInstance::getState));
			Map<String, ProcessInstance> processInstanceMap = new HashMap<String, ProcessInstance>();
			for (Map.Entry<State, List<ProcessInstance>> entry : processInstanceListMap.entrySet()) {
				processInstanceMap.put(entry.getKey().getState(), entry.getValue().get(0));
			}

			return processInstanceMap;

		} catch (Exception e) {
			Log.error(e.getMessage());
		}

		return null;
	}

	private List<List<LinkedHashMap>> getBoundaryData(String tenantId, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(fsmConfiguration.getLocationHost());
		uri.append(fsmConfiguration.getLocationContextPath()).append(fsmConfiguration.getLocationEndpoint());
		uri.append("?").append("tenantId=").append(tenantId);
		uri.append("&").append("boundaryType=").append("Locality");
		RequestInfoWrapper wrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		LinkedHashMap responseMap = (LinkedHashMap) serviceRequestRepository.fetchResult(uri, wrapper);

		if (CollectionUtils.isEmpty(responseMap)) {
			throw new CustomException(FSMErrorConstants.BOUNDARY_ERROR,
					"The response from location service is empty or null");
		}

		String jsonString = new JSONObject(responseMap).toString();

		DocumentContext context = JsonPath.parse(jsonString);

		List<List<LinkedHashMap>> boundaryResponse = context.read("$..boundary");

		return boundaryResponse;

	}

}
