package org.egov.bpa.service;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.IdGenRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.web.model.AuditDetails;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.idgen.IdResponse;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EnrichmentService {

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private BPAUtil bpaUtil;

	@Autowired
	private IdGenRepository idGenRepository;

	@Autowired
	private WorkflowService workflowService;

	public void enrichBPACreateRequest(BPARequest bpaRequest, Object mdmsData, Map<String, String> values) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), true);
		bpaRequest.getBPA().setAuditDetails(auditDetails);
		bpaRequest.getBPA().setId(UUID.randomUUID().toString());
		
		bpaRequest.getBPA().setAccountId(bpaRequest.getBPA().getAuditDetails().getCreatedBy());
		String applicationType = values.get("applicationType");
		if(applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN)){
		if(!bpaRequest.getBPA().getRiskType().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)){
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_MODULE_CODE);
		}else{
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_LOW_MODULE_CODE);
		}
		}else{
			bpaRequest.getBPA().setBusinessService(BPAConstants.BPA_OC_MODULE_CODE);
			bpaRequest.getBPA().setLandId(values.get("landId"));
		}
		if(bpaRequest.getBPA().getLandInfo()!=null){
			bpaRequest.getBPA().setLandId(bpaRequest.getBPA().getLandInfo().getId()); 
		}
		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		setIdgenIds(bpaRequest);
	}

	/**
	 * Sets the ApplicationNumber for given bpaRequest
	 *
	 * @param request
	 *            bpaRequest which is to be created
	 */
	private void setIdgenIds(BPARequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		String tenantId = request.getBPA().getTenantId();
		BPA bpa = request.getBPA();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNoIdgenName(),
				config.getApplicationNoIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);

		bpa.setApplicationNo(itr.next());
	}

	/**
	 * Returns a list of numbers generated from idgen
	 *
	 * @param requestInfo
	 *            RequestInfo from the request
	 * @param tenantId
	 *            tenantId of the city
	 * @param idKey
	 *            code of the field defined in application properties for which
	 *            ids are generated for
	 * @param idformat
	 *            format in which ids are to be generated
	 * @param count
	 *            Number of ids to be generated
	 * @return List of ids generated using idGen service
	 */
	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	public void enrichBPAUpdateRequest(BPARequest bpaRequest, BusinessService businessService) {

		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		AuditDetails auditDetails = bpaUtil.getAuditDetails(requestInfo.getUserInfo().getUuid(), false);
		auditDetails.setCreatedBy(bpaRequest.getBPA().getAuditDetails().getCreatedBy());
		auditDetails.setCreatedTime(bpaRequest.getBPA().getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
		
		// BPA Documents
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getDocuments()))
			bpaRequest.getBPA().getDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		// BPA WfDocuments
		if (!CollectionUtils.isEmpty(bpaRequest.getBPA().getWorkflow().getVarificationDocuments())) {
			bpaRequest.getBPA().getWorkflow().getVarificationDocuments().forEach(document -> {
				if (document.getId() == null) {
					document.setId(UUID.randomUUID().toString());
				}
			});
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void postStatusEnrichment(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();

		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		String state = workflowService.getCurrentState(bpa.getStatus(), businessService);

		if(state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)){
			bpa.setApplicationDate(Calendar.getInstance().getTimeInMillis());
		}
		
		if ((!bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)
				&& state.equalsIgnoreCase(BPAConstants.APPROVED_STATE))
				|| (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)
						&& bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE))) {
			int vailidityInMonths = config.getValidityInMonths();
			Calendar calendar = Calendar.getInstance();
			bpa.setApprovalDate(Calendar.getInstance().getTimeInMillis());

			// Adding 3years (36 months) to Current Date
			calendar.add(Calendar.MONTH, vailidityInMonths);
			Map<String, Object> additionalDetail = (Map) bpa.getAdditionalDetails();
			additionalDetail.put("validityDate", calendar.getTimeInMillis());
			List<IdResponse> idResponses = idGenRepository.getId(bpaRequest.getRequestInfo(), bpa.getTenantId(),
					config.getPermitNoIdgenName(), config.getPermitNoIdgenFormat(), 1).getIdResponses();
			bpa.setApprovalNo(idResponses.get(0).getId());
			if (state.equalsIgnoreCase(BPAConstants.DOCVERIFICATION_STATE)
					&& bpa.getRiskType().toString().equalsIgnoreCase(BPAConstants.LOW_RISKTYPE)) {
				
				Object mdmsData = bpaUtil.mDMSCall(bpaRequest.getRequestInfo(), bpaRequest.getBPA().getTenantId());
				String condeitionsPath = BPAConstants.CONDITIONS_MAP.replace("{1}", BPAConstants.PENDING_APPROVAL_STATE)
						.replace("{2}", bpa.getRiskType().toString())
						.replace("{3}", ((Map)bpa.getAdditionalDetails()).get("serviceType").toString())
						.replace("{4}", ((Map)bpa.getAdditionalDetails()).get("applicationType").toString());
				log.info(condeitionsPath);

				try {
					List<String> conditions = (List<String>) JsonPath.read(mdmsData, condeitionsPath);
					log.info(conditions.toString());
					if (bpa.getAdditionalDetails() == null) {
						bpa.setAdditionalDetails(new HashMap());
					}
					Map additionalDetails = (Map) bpa.getAdditionalDetails();
					additionalDetails.put(BPAConstants.PENDING_APPROVAL_STATE.toLowerCase(), conditions.get(0));

				} catch (Exception e) {
					log.warn("No approval conditions found for the application " + bpa.getApplicationNo());
				}
			}
		}
		
	}


}
