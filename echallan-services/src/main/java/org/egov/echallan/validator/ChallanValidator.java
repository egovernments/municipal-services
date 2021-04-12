package org.egov.echallan.validator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.Amount;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.Challan.StatusEnum;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.RequestInfoWrapper;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.egov.echallan.util.ChallanConstants.*;

@Component
@Slf4j
public class ChallanValidator {

	@Autowired
	private ChallanConfiguration config;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	
	public void validateFields(ChallanRequest request, Object mdmsData) {
		 Challan challan = request.getChallan();
		List<Map<String,Object>> taxPeriods = null;
         Map<String, String> errorMap = new HashMap<>();
         taxPeriods =  JsonPath.read(mdmsData, MDMS_FINACIALYEAR_PATH);
		 List<Amount> entAmount = challan.getAmount();
		 int totalAmt = 0;
		for (Amount amount : entAmount) {
			totalAmt+=amount.getAmount().intValue();
			if(amount.getAmount().compareTo(new BigDecimal(0)) == -1)
				errorMap.put("Negative Amount","Amount cannot be negative");
		}
		if(totalAmt <= 0) {
			errorMap.put("Zero amount","Challan cannot be generated for zero amount");
		}
		 if (challan.getCitizen().getMobileNumber() == null)
            errorMap.put("NULL_Mobile Number", " Mobile Number cannot be null");
         if (challan.getBusinessService() == null)
            errorMap.put("NULL_BusinessService", " Business Service cannot be null");
         if (challan.getTaxPeriodFrom() == null)
            errorMap.put("NULL_Fromdate", " From date cannot be null");
         if (challan.getTaxPeriodTo() == null)
            errorMap.put("NULL_Todate", " To date cannot be null");
         if(!challan.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
        	 errorMap.put("Invalid Tenant", "Invalid tenant id");

         Boolean validFinancialYear = false;
         if(challan.getTaxPeriodTo() != null && challan.getTaxPeriodFrom() != null){
			 for(Map<String,Object> financialYearProperties: taxPeriods){
				 Long startDate = (Long) financialYearProperties.get(MDMS_STARTDATE);
				 Long endDate = (Long) financialYearProperties.get(MDMS_ENDDATE);
				 if( challan.getTaxPeriodFrom() < challan.getTaxPeriodTo() && challan.getTaxPeriodFrom() >= startDate && challan.getTaxPeriodTo() <= endDate )
				 	validFinancialYear = true;
			 }
		 }

         if(!validFinancialYear)
			 errorMap.put("Invalid TaxPeriod", "Tax period details are invalid");

         List<String> localityCodes = getLocalityCodes(challan.getTenantId(), request.getRequestInfo());

         if(!localityCodes.contains(challan.getAddress().getLocality().getCode()))
         	errorMap.put("Invalid Locality", "Locality details are invalid");


		if (!errorMap.isEmpty())
        	 throw new CustomException(errorMap);
        
		
	}

	public List<String> getLocalityCodes(String tenantId, RequestInfo requestInfo){
		StringBuilder builder = new StringBuilder(config.getBoundaryHost());
		builder.append(config.getFetchBoundaryEndpoint());
		builder.append("?tenantId=");
		builder.append(tenantId);
		builder.append("&hierarchyTypeCode=");
		builder.append(HIERARCHY_CODE);
		builder.append("&boundaryType=");
		builder.append(BOUNDARY_TYPE);

		Object result = serviceRequestRepository.fetchResult(builder, new RequestInfoWrapper(requestInfo));

		List<String> codes = JsonPath.read(result, LOCALITY_CODE_PATH);
		return codes;
	}



	public void validateUpdateRequest(ChallanRequest request, List<Challan> searchResult) {
		Challan challan = request.getChallan();
		Map<String, String> errorMap = new HashMap<>();
		if (searchResult.size() == 0)
			errorMap.put("INVALID_UPDATE_REQ_NOT_EXIST", "The Challan to be updated is not in database");
		Challan searchchallan = searchResult.get(0);
		if(!challan.getBusinessService().equalsIgnoreCase(searchchallan.getBusinessService()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_BSERVICE", "The business service is not matching with the Search result");
		if(!challan.getChallanNo().equalsIgnoreCase(searchchallan.getChallanNo()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_CHALLAN_NO", "The Challan Number is not matching with the Search result");
		if(!challan.getAddress().getId().equalsIgnoreCase(searchchallan.getAddress().getId()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_ADDRESS", "Address is not matching with the Search result");
		if(!challan.getCitizen().getUuid().equalsIgnoreCase(searchchallan.getCitizen().getUuid()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_ADDRESS", "User Details not matching with the Search result");
		if(!challan.getCitizen().getName().equalsIgnoreCase(searchchallan.getCitizen().getName()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_NAME", "User Details not matching with the Search result");
		if(!challan.getCitizen().getMobileNumber().equalsIgnoreCase(searchchallan.getCitizen().getMobileNumber()))
			errorMap.put("INVALID_UPDATE_REQ_NOTMATCHED_MOBILENO", "User Details not matching with the Search result");
		if(searchchallan.getApplicationStatus()!=StatusEnum.ACTIVE)
			errorMap.put("INVALID_UPDATE_REQ_CHALLAN_INACTIVE", "Challan cannot be updated/cancelled");
		if(!challan.getTenantId().equalsIgnoreCase(request.getRequestInfo().getUserInfo().getTenantId()))
       	 	errorMap.put("INVALID_UPDATE_REQ_INVALID_TENANTID", "Invalid tenant id");
		if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
		
	}
}
