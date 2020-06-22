package org.egov.pt.web.controllers;

import java.util.Arrays;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.AssessmentSearchCriteria;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.service.AssessmentService;
import org.egov.pt.service.PropertyService;
import org.egov.pt.util.ResponseInfoFactory;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.egov.pt.web.contracts.AssessmentResponse;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.pt.web.contracts.PropertyResponse;
import org.egov.pt.web.contracts.RequestInfoWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/assessment")
public class AssessmentController {
	
	@Autowired
	private AssessmentService assessmentService;

	@Autowired
	private ResponseInfoFactory responseInfoFactory;

	@PostMapping("/_create")
	public ResponseEntity<AssessmentResponse> create(@Valid @RequestBody AssessmentRequest assessmentRequest) {

		Assessment assessment = assessmentService.createAssessment(assessmentRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(assessmentRequest.getRequestInfo(), true);
		AssessmentResponse response = AssessmentResponse.builder()
				.assessments(Arrays.asList(assessment))
				.responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	
	@PostMapping("/_update")
	public ResponseEntity<AssessmentResponse> update(@Valid @RequestBody AssessmentRequest assessmentRequest) {
		
		Assessment assessment = assessmentService.updateAssessment(assessmentRequest);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(assessmentRequest.getRequestInfo(), true);
		AssessmentResponse response = AssessmentResponse.builder()
				.assessments(Arrays.asList(assessment))
				.responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@PostMapping("/_search")
	public ResponseEntity<AssessmentResponse> search(@Valid @RequestBody RequestInfoWrapper requestInfoWrapper,
			@Valid @ModelAttribute AssessmentSearchCriteria assessmentSearchCriteria) {
		List<Assessment> assessments = assessmentService.searchAssessments(requestInfoWrapper.getRequestInfo(), assessmentSearchCriteria);
		ResponseInfo resInfo = responseInfoFactory.createResponseInfoFromRequestInfo(requestInfoWrapper.getRequestInfo(), true);
		AssessmentResponse response = AssessmentResponse.builder()
				.assessments(assessments)
				.responseInfo(resInfo)
				.build();
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
