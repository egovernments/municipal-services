package org.egov.pt.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Assessment;
import org.egov.pt.models.AssessmentSearchCriteria;
import org.egov.pt.models.AuditDetails;
import org.egov.pt.models.Document;
import org.egov.pt.models.Unit;
import org.egov.pt.models.enums.DocumentBelongsTo;
import org.egov.pt.models.enums.Status;
import org.egov.pt.producer.Producer;
import org.egov.pt.validator.AssessmentValidator;
import org.egov.pt.web.contracts.AssessmentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class AssessmentService {
	
	@Autowired
	private AssessmentValidator validator;
	
	@Autowired
	private Producer producer;
	
	@Autowired
	private PropertyConfiguration props;
	
	
	public Assessment createAssessment(AssessmentRequest request) {
		validator.validateAssessmentCreate(request);
		enrichAssessmentCreate(request);
		producer.push(props.getCreateAssessmentTopic(), request);
		
		return request.getAssessment();
	}
	
	public Assessment updateAssessment(AssessmentRequest request) {
		validator.validateAssessmentUpdate(request);
		enrichAssessmentUpdate(request);
		producer.push(props.getUpdateAssessmentTopic(), request);

		return request.getAssessment();
	}
	
	private void enrichAssessmentCreate(AssessmentRequest request) {
		Assessment assessment = request.getAssessment();
		assessment.setId(String.valueOf(UUID.randomUUID()));
		assessment.setAssessmentNumber("");
		assessment.setStatus(Status.ACTIVE);
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(request.getRequestInfo().getUserInfo().getUuid())
				.createdTime(new Date().getTime())
				.lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
				.lastModifiedTime(new Date().getTime()).build();
		
		if(!CollectionUtils.isEmpty(assessment.getUnits())) {
			for(Unit unit: assessment.getUnits()) {
				unit.setId(String.valueOf(UUID.randomUUID()));
				unit.setAssessmentId(assessment.getId());
				unit.setActive(true);
				unit.setAuditDetails(auditDetails);
			}
		}		
		if(!CollectionUtils.isEmpty(assessment.getDocuments())) {
			for(Document doc: assessment.getDocuments()) {
				doc.setId(String.valueOf(UUID.randomUUID()));
				doc.setEntityId(assessment.getId());
				doc.setAuditDetails(auditDetails);
			}
		}		
		
		assessment.setAuditDetails(auditDetails);
		
	}
	
	
	private void enrichAssessmentUpdate(AssessmentRequest request) {
		Assessment assessment = request.getAssessment();
		
		AuditDetails auditDetails = AuditDetails.builder()
				.createdBy(request.getRequestInfo().getUserInfo().getUuid())
				.createdTime(new Date().getTime())
				.lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid())
				.lastModifiedTime(new Date().getTime()).build();
		
		if(!CollectionUtils.isEmpty(assessment.getUnits())) {
			for(Unit unit: assessment.getUnits()) {
				if(StringUtils.isEmpty(unit.getId())) {
					unit.setId(String.valueOf(UUID.randomUUID()));
					unit.setAssessmentId(assessment.getId());
					unit.setActive(true);
					unit.setAuditDetails(auditDetails);
				}
			}
		}
		if(!CollectionUtils.isEmpty(assessment.getDocuments())) {
			for(Document doc: assessment.getDocuments()) {
				if(StringUtils.isEmpty(doc.getId())) {
					doc.setId(String.valueOf(UUID.randomUUID()));
					doc.setEntityId(assessment.getId());
					doc.setAuditDetails(auditDetails);
				}
			}
		}
		
		assessment.getAuditDetails().setLastModifiedBy(auditDetails.getLastModifiedBy());
		assessment.getAuditDetails().setLastModifiedTime(auditDetails.getLastModifiedTime());
		
		
	}
	
	public List<Assessment> searchAssessments(RequestInfo requestInfo, AssessmentSearchCriteria criteria) {
		return null;
	}
}
