package org.egov.noc.validator;

import java.util.HashMap;
import java.util.Map;

import org.egov.noc.util.NOCConstants;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class NOCValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	
	public void validateCreate(NocRequest nocRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(nocRequest, mdmsData);
	}
	
	public void validateUpdate(NocRequest nocRequest, Noc searchResult, Object mdmsData) {
		Noc noc = nocRequest.getNoc();
		validateData(searchResult, noc);
		mdmsValidator.validateMdmsData(nocRequest, mdmsData);
	}
	
	private void validateData(Noc searchResult, Noc noc) {
		Map<String, String> errorMap = new HashMap<>();
		
		if (noc.getId() == null) {
			errorMap.put("UPDATE ERROR", "Application Not found in the System" + noc);
		}
		
		if (noc.getWorkflow().getComment() == null || noc.getWorkflow().getComment().isEmpty()) {
			errorMap.put("NOC_UPDATE_ERROR_COMMENT_REQUIRED",
					"Comment is mandaotory, please provide the comments ");
		}
		
		if (noc.getWorkflow().getAction().equalsIgnoreCase(NOCConstants.ACTION_APPROVE) 
				&& (noc.getDocuments() == null || noc.getDocuments().isEmpty())) {
			errorMap.put("NOC_UPDATE_ERROR_DOCUMENT_REQUIRED", "Noc document is mandatory, please provide the documents.");
		}

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
	
}
