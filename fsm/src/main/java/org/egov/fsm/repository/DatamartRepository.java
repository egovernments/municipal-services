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

import org.egov.fsm.web.model.DataMartModel;
import org.egov.fsm.web.model.DataMartTenantModel;
import org.egov.fsm.web.model.RequestInfoWrapper;
import org.egov.fsm.web.model.location.Boundary;
import org.egov.fsm.web.model.workflow.ProcessInstance;
import org.egov.fsm.web.model.workflow.ProcessInstanceResponse;
import org.egov.fsm.web.model.workflow.State;
import org.egov.fsm.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
import org.json.JSONObject;

@Repository
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

		StringBuilder query = new StringBuilder(DataMartQueryBuilder.dataMartQuery);
		List<DataMartModel> datamartList = new ArrayList<DataMartModel>();
		for (DataMartTenantModel tenantModel : totalrowsWithTenantId) {
			List<List<Boundary>> boundaryData = getBoundaryData(tenantModel.getTenantId(), requestInfo);
			ObjectMapper objectMapper = new ObjectMapper();
			String jsonString = new JSONObject(boundaryData.get(0)).toString();
			List<Boundary> boundaryObject = objectMapper.convertValue(jsonString, new TypeReference<List<Boundary>>() {
			});

			Object mdmsData = dataMartUtil.mDMSCall(requestInfo, tenantModel.getTenantId());
			Map<String, JsonNode> masterData = dataMartUtil.groupMdmsDataByMater(mdmsData);
			for (int i = 0; i < tenantModel.getCount() - 500; i += 500) {
				query.append(" offset " + i + " limit 500 ;");
				List<DataMartModel> dataMartList = jdbcTemplate.query(query.toString(), dataMartRowMapper);
				for (DataMartModel dataMartModel : dataMartList) {
					String locality = dataMartModel.getLocality();
					Map<String, ProcessInstance> processInstanceData = getProceessInstanceData(
							dataMartModel.getApplicationId(), requestInfo, totalrowsWithTenantId.get(i).getTenantId());
					dataMartModel = enrichWorkFlowData(processInstanceData, dataMartModel);

					if (dataMartModel.getLocality() != null && boundaryData != null) {
						System.out.println(boundaryData.get(0).get(0));
						List<Boundary> boundaryResponse = boundaryObject.stream()
								.filter(boundary -> boundary.getCode() == locality).collect(Collectors.toList());
						dataMartModel.setLocality(boundaryResponse.get(0).getName());

					}

					// if(dataMartModel.getSlumName()!=null) {
					JsonNode slumMasterData = masterData.get(FSMConstants.MDMS_SLUM_NAME);

					// }

					datamartList.add(dataMartModel);
				}
			}
		}
		return datamartList;

	}

	private DataMartModel enrichWorkFlowData(Map<String, ProcessInstance> processInstanceData,
			DataMartModel dataMartModel) {

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

	}

	private List<List<Boundary>> getBoundaryData(String tenantId, RequestInfo requestInfo) {
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

		List<List<Boundary>> boundaryResponse = context.read("$..boundary");

		return boundaryResponse;

	}

}
