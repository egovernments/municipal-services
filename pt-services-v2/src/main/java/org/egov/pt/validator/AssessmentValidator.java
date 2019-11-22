package org.egov.pt.validator;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.AssessmentSearchCriteria;
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
		validateDataSanity(assessmentRequest.getAssessment(), errorMap);
		validateMDMSData(assessmentRequest.getAssessment(), errorMap);
	}
	
	public void validateAssessmentUpdate(AssessmentRequest assessmentRequest) {
		Map<String, String> errorMap = new HashMap<>();
		validateRI(assessmentRequest.getRequestInfo(), errorMap);
		validateIfAssessmentExists(assessmentRequest, errorMap);
		validateDataSanity(assessmentRequest.getAssessment(), errorMap);
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
	
	private void validateIfAssessmentExists(AssessmentRequest assessmentRequest, Map<String, String> errorMap) {
		Assessment assessment = assessmentRequest.getAssessment();
		Set<String> ids = new HashSet<>();
		ids.add(assessment.getId());
		AssessmentSearchCriteria criteria = AssessmentSearchCriteria.builder().ids(ids).build();
		List<Assessment> assessments = assessmentService.searchAssessments(assessmentRequest.getRequestInfo(), criteria);
		
		if(CollectionUtils.isEmpty(assessments)) {
			errorMap.put(ErrorConstants.NO_ASSESSMENTS_FOUND_CODE, ErrorConstants.NO_ASSESSMENTS_FOUND_MSG);
		}
		
		if (!CollectionUtils.isEmpty(errorMap.keySet())) {
			throw new CustomException(errorMap);
		}
	}
	
	
	private void validateDataSanity(Assessment assessment, Map<String, String> errorMap) {
		
		if(assessment.getAssessmentDate() > new Date().getTime()) {
			errorMap.put(ErrorConstants.ASSMENT_DATE_FUTURE_ERROR_CODE, ErrorConstants.ASSMENT_DATE_FUTURE_ERROR_MSG);
		}
		
		//search property on id and check if the property exists.

		
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
