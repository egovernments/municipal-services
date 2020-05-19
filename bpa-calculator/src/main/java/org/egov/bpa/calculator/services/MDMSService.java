package org.egov.bpa.calculator.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.bpa.calculator.config.BPACalculatorConfig;
import org.egov.bpa.calculator.repository.ServiceRequestRepository;
import org.egov.bpa.calculator.utils.BPACalculatorConstants;
import org.egov.bpa.calculator.web.models.CalculationReq;
import org.egov.bpa.calculator.web.models.bpa.BPA;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MDMSService {

	@Autowired
	 private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private BPACalculatorConfig config;
	
    public Object mDMSCall(CalculationReq calculationReq,String tenantId){
        MdmsCriteriaReq mdmsCriteriaReq = getMDMSRequest(calculationReq,tenantId);
        StringBuilder url = getMdmsSearchUrl();
        Object result = serviceRequestRepository.fetchResult(url , mdmsCriteriaReq);
        return result;
    }

    /**
     * Creates and returns the url for mdms search endpoint
     *
     * @return MDMS Search URL
     */
    private StringBuilder getMdmsSearchUrl() {
        return new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsSearchEndpoint());
    }

    /**
     * Creates MDMS request
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param tenantId The tenantId of the tradeLicense
     * @return MDMSCriteria Request
     */
    private MdmsCriteriaReq getMDMSRequest(CalculationReq calculationReq, String tenantId) {
    	RequestInfo requestInfo = 	calculationReq.getRequestInfo();
        List<MasterDetail> bpaMasterDetails = new ArrayList<>();
        
        bpaMasterDetails.add(MasterDetail.builder().name(BPACalculatorConstants.MDMS_CALCULATIONTYPE)
        		.build());
        ModuleDetail bpaModuleDtls = ModuleDetail.builder().masterDetails(bpaMasterDetails)
                .moduleName(BPACalculatorConstants.MDMS_BPA).build();

        List<ModuleDetail> moduleDetails = new ArrayList<>();
        
        moduleDetails.add(bpaModuleDtls);

        MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
                .build();

        return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
    }


    /**
     * Gets the calculationType for the city for a particular financialYear
     * If for particular financialYear entry is not there previous year is taken
     * If MDMS data is not available default values are returned
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param license The tradeLicense for which calculation is done
     * @return Map contianing the calculationType for TradeUnit and accessory
     */
    @SuppressWarnings("rawtypes")
	public Map getCalculationType(RequestInfo requestInfo,BPA bpa,Object mdmsData, String feeType){
        HashMap<String,Object> calculationType = new HashMap<>();
        try {
        	
           

            List jsonOutput = JsonPath.read(mdmsData, BPACalculatorConstants.MDMS_CALCULATIONTYPE_PATH);
            String filterExp = "$.[?((@.applicationType == '"+((Map)bpa.getAdditionalDetails()).get("applicationType")+"' || @.applicationType === 'ALL' ) &&  @.feeType == '"+feeType+"')]";
            List<Object> calTypes = JsonPath.read(jsonOutput, filterExp);
            
            filterExp = "$.[?(@.serviceType == '"+((Map)bpa.getAdditionalDetails()).get("serviceType")+"' || @.serviceType === 'ALL' )]";
            calTypes = JsonPath.read(calTypes, filterExp);
            
            filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' || @.riskType === 'ALL' )]";
            calTypes = JsonPath.read(calTypes, filterExp);
            
            if(calTypes.size() > 1){
            	
	            	filterExp = "$.[?(@.riskType == '"+bpa.getRiskType()+"' )]";
	            	calTypes  = JsonPath.read(calTypes, filterExp);
	            	
            }
            
            if(calTypes.size() == 0) {
            		return defaultMap(feeType);
            }
            
             Object obj = calTypes.get(0);
           
          
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            
           String financialYear = currentYear + "-" + (currentYear + 1);
           System.out.println(financialYear);
            
            calculationType = (HashMap<String, Object>) obj;
        }
        catch (Exception e){
            throw new CustomException("MDMS ERROR","Failed to get calculationType");
        }

        return calculationType;
    }
   

    /**
     * Creates and return default calculationType values as map
     * @return default calculationType Map
     */
    private Map defaultMap(String feeType){
        Map defaultMap = new HashMap();
        String feeAmount = ( feeType.equalsIgnoreCase(BPACalculatorConstants.MDMS_CALCULATIONTYPE_APL_FEETYPE) ) ? config.getApplFeeDefaultAmount() : config.getSancFeeDefaultAmount();
        defaultMap.put( BPACalculatorConstants.MDMS_CALCULATIONTYPE_AMOUNT,feeAmount);
        return defaultMap;
    }


    /**
     * Gets the startDate and the endDate of the financialYear
     * @param requestInfo The RequestInfo of the calculationRequest
     * @param license The tradeLicense for which calculation is done
     * @return Map containing the startDate and endDate
     */
   /* public Map<String,Long> getTaxPeriods(RequestInfo requestInfo,BPA bpa,Object mdmsData){
        Map<String,Long> taxPeriods = new HashMap<>();
        try {
            String jsonPath = BPACalculatorConstants.MDMS_FINACIALYEAR_PATH.replace("{}","2019-20");
            List<Map<String,Object>> jsonOutput =  JsonPath.read(mdmsData, jsonPath);
            Map<String,Object> financialYearProperties = jsonOutput.get(0);
            Object startDate = financialYearProperties.get(BPACalculatorConstants.MDMS_STARTDATE);
            Object endDate = financialYearProperties.get(BPACalculatorConstants.MDMS_ENDDATE);
            taxPeriods.put(BPACalculatorConstants.MDMS_STARTDATE,(Long) startDate);
            taxPeriods.put(BPACalculatorConstants.MDMS_ENDDATE,(Long) endDate);

        } catch (Exception e) {
            log.error("Error while fetvhing MDMS data", e);
            throw new CustomException("INVALID FINANCIALYEAR", "No data found for the financialYear: "+"2019-20");
        }
        return taxPeriods;
    }*/

}
