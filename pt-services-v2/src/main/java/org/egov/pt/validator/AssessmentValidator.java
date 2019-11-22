package org.egov.pt.validator;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.AssessmentSearchCriteria;
import org.egov.pt.models.Document;
import org.egov.pt.models.Unit;
import org.egov.pt.models.enums.DocumentBelongsTo;
import org.egov.pt.service.AssessmentService;
import org.egov.pt.util.ErrorConstants;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

public class AssessmentValidator {
	
	@Autowired
	private AssessmentService assessmentService;
	
	
	public void validateAssessmentCreate(AssessmentRequest assessmentRequest) {
		Map<String, String> errorMap = new HashMap<>();
		validateRI(assessmentRequest.getRequestInfo(), errorMap);
		commonValidations(assessmentRequest.getAssessment(), errorMap, false);
		validateMDMSData(assessmentRequest.getAssessment(), errorMap);
	}
	
	public void validateAssessmentUpdate(AssessmentRequest assessmentRequest) {
		Map<String, String> errorMap = new HashMap<>();
		validateRI(assessmentRequest.getRequestInfo(), errorMap);
		validateUpdateRequest(assessmentRequest, errorMap);
		commonValidations(assessmentRequest.getAssessment(), errorMap, true);
		validateMDMSData(assessmentRequest.getAssessment(), errorMap);
	}
	
	/**
	 * Method to validate the necessary RI details.
	 * 
	 * @param requestInfo
	 * @param errorMap
	 */
	private void validateRI(RequestInfo requestInfo, Map<String, String> errorMap) {
		if (null != requestInfo) {
			if(null != requestInfo.getUserInfo()) {
				if ((StringUtils.isEmpty(requestInfo.getUserInfo().getUuid()))
						|| (CollectionUtils.isEmpty(requestInfo.getUserInfo().getRoles()))
						|| (StringUtils.isEmpty(requestInfo.getUserInfo().getTenantId()))) {
					errorMap.put(ErrorConstants.MISSING_ROLE_USERID_CODE, ErrorConstants.MISSING_ROLE_USERID_MSG);
				}
			}else {
				errorMap.put(ErrorConstants.MISSING_USR_INFO_CODE, ErrorConstants.MISSING_USR_INFO_MSG);
			}

		} else {
			errorMap.put(ErrorConstants.MISSING_REQ_INFO_CODE, ErrorConstants.MISSING_REQ_INFO_MSG);
		}
		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}

	}
	
	private void validateUpdateRequest(AssessmentRequest assessmentRequest, Map<String, String> errorMap) {
		Assessment assessment = assessmentRequest.getAssessment();
		if(StringUtils.isEmpty(assessment.getId())) {
			errorMap.put("ASSMNT_ID_EMPTY", "Assessment ID cannot be empty");
		}
		Set<String> ids = new HashSet<>();
		ids.add(assessment.getId());
		AssessmentSearchCriteria criteria = AssessmentSearchCriteria.builder().ids(ids).build();
		List<Assessment> assessments = assessmentService.searchAssessments(assessmentRequest.getRequestInfo(), criteria);
		if(CollectionUtils.isEmpty(assessments)) {
			errorMap.put(ErrorConstants.NO_ASSESSMENTS_FOUND_CODE, ErrorConstants.NO_ASSESSMENTS_FOUND_MSG);
		}else {
			Assessment assessmentFromDB = assessments.get(0);
			if(assessmentFromDB.getDocuments().size() > assessment.getDocuments().size()) {
				errorMap.put("MISSING_DOCUMENTS", "Please send all the documents belonging to this assessment");
			}
			if(assessmentFromDB.getUnits().size() > assessment.getUnits().size()) {
				errorMap.put("MISSING_UNITS", "Please send all the units belonging to this assessment");
			}
			Set<String> existingUnits = assessmentFromDB.getUnits().stream().map(Unit :: getId).collect(Collectors.toSet());
			Set<String> existingDocs = assessmentFromDB.getDocuments().stream().map(Document :: getId).collect(Collectors.toSet());
			if(!CollectionUtils.isEmpty(assessment.getUnits())) {
				for(Unit unit: assessment.getUnits()) {
					if(!StringUtils.isEmpty(unit.getId())) {
						if(!existingUnits.contains(unit.getId())) {
							errorMap.put("UNIT_NOT_FOUND", "You're trying to update a non-existent unit: "+unit.getId());
						}		
					}
				}	
			}
			if(!CollectionUtils.isEmpty(assessment.getDocuments())) {
				for(Document doc: assessment.getDocuments()) {
					if(!StringUtils.isEmpty(doc.getId())) {
						if(!existingDocs.contains(doc.getId())) {
							errorMap.put("DOC_NOT_FOUND", "You're trying to update a non-existent document: "+doc.getId());
						}
					}
				}
			}

		}
		
		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
	}
	
	
	private void commonValidations(Assessment assessment, Map<String, String> errorMap, Boolean isUpdate) {
			
		//search property on id and check if the property exists.
		
		if(assessment.getAssessmentDate() > new Date().getTime()) {
			errorMap.put(ErrorConstants.ASSMENT_DATE_FUTURE_ERROR_CODE, ErrorConstants.ASSMENT_DATE_FUTURE_ERROR_MSG);
		}
		
		if(isUpdate) {
			if(null == assessment.getStatus()) {
				errorMap.put("ASSMNT_STATUS_EMPTY", "Assessment Status cannot be empty");
			}
		}
		
		else {
			
		}
				
		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
		
	}
	
	private void validateMDMSData(Assessment assessment, Map<String, String> errorMap) {
		
		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
		
	}

}
