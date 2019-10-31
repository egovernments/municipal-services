package org.egov.waterConnection.validator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.repository.ServiceRequestRepository;
import org.egov.waterConnection.util.WCConstants;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MDMSValidator {
	@Autowired
	private WaterServicesUtil waterServicesUtil;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndpoint;

	public void validateMasterData(WaterConnectionRequest request) {
		Map<String, String> errorMap = new HashMap<>();
		String tenantId = request.getWaterConnection().getProperty().getTenantId();

		String[] masterNames = { WCConstants.MDMS_WC_Connection_Type, WCConstants.MDMS_WC_Connection_Category,
				WCConstants.MDMS_WC_Water_Source };
		List<String> names = new ArrayList<>(Arrays.asList(masterNames));
		Map<String, List<String>> codes = getAttributeValues(tenantId, WCConstants.MDMS_WC_MOD_NAME, names, "$.*.code",
				WCConstants.JSONPATH_CODES, request.getRequestInfo());
		 validateMDMSData(masterNames, codes);
		 validateCodes(request.getWaterConnection(),codes,errorMap);

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	
	
	   private static Map<String,String> validateCodes(WaterConnection waterConnection,Map<String,List<String>> codes,Map<String,String> errorMap){
	        log.info("Validating WaterConnection");
//	        properties.forEach(property -> {
//	            property.getPropertyDetails().forEach(propertyDetail -> {
//
//	                if(!codes.get(PTConstants.MDMS_PT_PROPERTYTYPE).contains(propertyDetail.getPropertyType()) && propertyDetail.getPropertyType()!=null){
//	                    errorMap.put("Invalid PROPERTYTYPE","The PropertyType '"+propertyDetail.getPropertyType()+"' does not exists");
//	                }
//
//	                if(!codes.get(PTConstants.MDMS_PT_SUBOWNERSHIP).contains(propertyDetail.getSubOwnershipCategory()) && propertyDetail.getSubOwnershipCategory()!=null){
//	                    errorMap.put("Invalid SUBOWNERSHIPCATEGORY","The SubOwnershipCategory '"+propertyDetail.getSubOwnershipCategory()+"' does not exists");
//	                }
//
//	                if(!codes.get(PTConstants.MDMS_PT_OWNERSHIP).contains(propertyDetail.getOwnershipCategory()) && propertyDetail.getOwnershipCategory()!=null){
//	                    errorMap.put("Invalid OWNERSHIPCATEGORY","The OwnershipCategory '"+propertyDetail.getOwnershipCategory()+"' does not exists");
//	                }
//
//	                if(!codes.get(PTConstants.MDMS_PT_PROPERTYSUBTYPE).contains(propertyDetail.getPropertySubType()) && propertyDetail.getPropertySubType()!=null){
//	                    errorMap.put(ErrorConstants.INVALID_PROPERTYSUBTYPE,"The PropertySubType '"+propertyDetail.getPropertySubType()+"' does not exists");
//	                }
//
//	                if(!codes.get(PTConstants.MDMS_PT_USAGEMAJOR).contains(propertyDetail.getUsageCategoryMajor()) && propertyDetail.getUsageCategoryMajor()!=null){
//	                    errorMap.put("INVALID USAGECATEGORYMAJOR","The UsageCategoryMajor '"+propertyDetail.getUsageCategoryMajor()+"' at Property level does not exists");
//	                }
//
//	                if(!CollectionUtils.isEmpty(propertyDetail.getUnits()))
//	                    propertyDetail.getUnits().forEach(unit ->{
//	                        if(!codes.get(PTConstants.MDMS_PT_USAGEMAJOR).contains(unit.getUsageCategoryMajor()) && unit.getUsageCategoryMajor()!=null){
//	                            errorMap.put("INVALID USAGECATEGORYMAJOR","The UsageCategoryMajor '"+unit.getUsageCategoryMajor()+"' at unit level does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_USAGEMINOR).contains(unit.getUsageCategoryMinor()) && unit.getUsageCategoryMinor()!=null){
//	                            errorMap.put("INVALID USAGECATEGORYMINOR","The UsageCategoryMinor '"+unit.getUsageCategoryMinor()+"' does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_USAGESUBMINOR).contains(unit.getUsageCategorySubMinor()) && unit.getUsageCategorySubMinor()!=null){
//	                            errorMap.put("INVALID USAGECATEGORYSUBMINOR","The UsageCategorySubMinor '"+unit.getUsageCategorySubMinor()+"' does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_USAGEDETAIL).contains(unit.getUsageCategoryDetail()) && unit.getUsageCategoryDetail()!=null){
//	                            errorMap.put("INVALID USAGECATEGORYDETAIL","The UsageCategoryDetail "+unit.getUsageCategoryDetail()+" does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_CONSTRUCTIONTYPE).contains(unit.getConstructionType()) && unit.getConstructionType()!=null){
//	                            errorMap.put("INVALID CONSTRUCTIONTYPE","The ConstructionType '"+unit.getConstructionType()+"' does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_CONSTRUCTIONSUBTYPE).contains(unit.getConstructionSubType()) && unit.getConstructionSubType()!=null){
//	                            errorMap.put("INVALID CONSTRUCTIONSUBTYPE","The ConstructionSubType '"+unit.getConstructionSubType()+"' does not exists");
//	                        }
//
//	                        if(!codes.get(PTConstants.MDMS_PT_OCCUPANCYTYPE).contains(unit.getOccupancyType()) && unit.getOccupancyType()!=null){
//	                            errorMap.put("INVALID OCCUPANCYTYPE","The OccupancyType '"+unit.getOccupancyType()+"' does not exists");
//	                        }
//
//	                        if("RENTED".equalsIgnoreCase(unit.getOccupancyType())){
//	                            if(unit.getArv()==null || unit.getArv().compareTo(new BigDecimal(0))!=1)
//	                                errorMap.put("INVALID ARV","Total Annual Rent should be greater than zero ");
//	                        }
//	                    });
//
//	                propertyDetail.getOwners().forEach(owner ->{
//	                    if(!codes.get(PTConstants.MDMS_PT_OWNERTYPE).contains(owner.getOwnerType()) && owner.getOwnerType()!=null){
//	                        errorMap.put("INVALID OWNERTYPE","The OwnerType '"+owner.getOwnerType()+"' does not exists");
//	                    }
//	                });
//	            });
//
//	        });
	        return errorMap;

	    }

	private Map<String, List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names,
			String filter, String jsonpath, RequestInfo requestInfo) {
		StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
		MdmsCriteriaReq criteriaReq = waterServicesUtil.prepareMdMsRequest(tenantId, moduleName, names, filter,
				requestInfo);
		try {
			Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
			return JsonPath.read(result, jsonpath);
		} catch (Exception e) {
			log.error("Error while fetching MDMS data", e);
			throw new CustomException(WCConstants.INVALID_CONNECTION_CATEGORY, WCConstants.INVALID_CONNECTION_TYPE);
		}
	}

	private void validateMDMSData(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

}
