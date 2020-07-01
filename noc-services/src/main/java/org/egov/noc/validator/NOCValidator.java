package org.egov.noc.validator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.noc.config.NOCConfiguration;
import org.egov.noc.util.NOCConstants;
import org.egov.noc.web.model.Noc;
import org.egov.noc.web.model.NocRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

@Component
public class NOCValidator {

	@Autowired
	private MDMSValidator mdmsValidator;
	
	@Autowired
	private NOCConfiguration nocConfiguration;

	
	public void validateCreate(NocRequest nocRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(nocRequest, mdmsData);
	}
	
	public void validateUpdate(NocRequest nocRequest, Noc searchResult, String mode, Object mdmsData) {
		Noc noc = nocRequest.getNoc();
		validateData(searchResult, noc, mode);
		mdmsValidator.validateMdmsData(nocRequest, mdmsData);
	}
	
	private void validateData(Noc searchResult, Noc noc, String mode) {
		Map<String, String> errorMap = new HashMap<>();
		
		if (noc.getId() == null) {
			errorMap.put("UPDATE ERROR", "Application Not found in the System" + noc);
		}
		
		if (noc.getWorkflow().getAction().equalsIgnoreCase(NOCConstants.ACTION_APPROVE)  
				&& (noc.getDocuments() == null || noc.getDocuments().isEmpty()) && (mode.equals(NOCConstants.ONLINE_MODE) 
				|| (mode.equals(NOCConstants.OFFLINE_MODE) && nocConfiguration.getNocOfflineDocRequired()))) {
			errorMap.put("NOC_UPDATE_ERROR_DOCUMENT_REQUIRED", "Noc document is mandatory, please provide the documents.");
		}
		

		if (noc.getWorkflow().getAction().equalsIgnoreCase(NOCConstants.ACTION_REJECT) && 
				(noc.getWorkflow().getComment() == null || noc.getWorkflow().getComment().isEmpty()))
	        errorMap.put("NOC_UPDATE_ERROR_COMMENT_REQUIRED", "Comment is mandaotory, please provide the comments ");

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}
	
	public Map<String, String> getOrValidateBussinessService(Noc noc, Object mdmsData) {
		List<Map<String, Object>>  result = JsonPath.read(mdmsData, NOCConstants.NOCTYPE_JSONPATH_CODE);		
		if(result.isEmpty()){
			throw new CustomException("MDMS DATA ERROR", "Unable to fetch NocType from MDMS");
		}
		
    	String filterExp = "$.[?(@.code == '"+noc.getNocType()+"' )]";
        List<Map<String, Object>> jsonOutput = JsonPath.read(result, filterExp);        
		if(jsonOutput.isEmpty()){
			throw new CustomException("MDMS DATA ERROR", "Unable to fetch " + noc.getNocType() + " workflow mode from MDMS");
		}
       
		Map<String, String> businessValues = new HashMap<>();	
		businessValues.put(NOCConstants.MODE, (String) jsonOutput.get(0).get(NOCConstants.MODE));
		if(jsonOutput.get(0).get(NOCConstants.MODE).equals(NOCConstants.ONLINE_MODE))
		    businessValues.put(NOCConstants.WORKFLOWCODE, (String)jsonOutput.get(0).get(NOCConstants.ONLINE_WF)); 
		 else
		    businessValues.put(NOCConstants.WORKFLOWCODE, (String)  jsonOutput.get(0).get(NOCConstants.OFFLINE_WF));
		  
	    noc.setAdditionalDetails(businessValues);		 
	    return businessValues;
    }
	
}
