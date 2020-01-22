package org.egov.bpa.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.models.BPARequest;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.jayway.jsonpath.JsonPath;

@Component
@Slf4j
public class MDMSValidator {

	/**
	 * method to validate the mdms data in the request
	 *
	 * @param bpaRequest
	 */
	public void validateMdmsData(BPARequest bpaRequest, Object mdmsData) {

		Map<String, String> errorMap = new HashMap<>();

		  Map<String, List<String>> masterData = getAttributeValues(mdmsData);
	        String[] masterArray = { BPAConstants.SERVICE_TYPE, BPAConstants.APPLICATION_TYPE,
	                                 BPAConstants.OWNERSHIP_CATEGORY, BPAConstants.OWNER_TYPE, BPAConstants.OCCUPANCY_TYPE,BPAConstants.SUB_OCCUPANCY_TYPE,BPAConstants.USAGES};

	        validateIfMasterPresent(masterArray, masterData);
		
		bpaRequest.getBPA().getOwners().forEach(owner -> {
			if (owner.getOwnerType() == null) {
				owner.setOwnerType("NONE");
			}
		});
	            if(!masterData.get(BPAConstants.OWNERSHIP_CATEGORY)
	                    .contains(bpaRequest.getBPA().getOwnershipCategory()))
	                errorMap.put("INVALID OWNERSHIPCATEGORY", "The OwnerShipCategory '"
	                        + bpaRequest.getBPA().getOwnershipCategory() + "' does not exists");

	            if(!masterData.get(BPAConstants.SERVICE_TYPE).
	                    contains(bpaRequest.getBPA().getServiceType()))
	                errorMap.put("INVALID SERVICETYPE", "The ServiceType '"
	                        + bpaRequest.getBPA().getServiceType() + "' does not exists");
	            
	            
	            if(!masterData.get(BPAConstants.APPLICATION_TYPE).
	                    contains(bpaRequest.getBPA().getApplicationType()))
	                errorMap.put("INVALID APPLICATIONTYPE", "The ApplicationType '"
	                        + bpaRequest.getBPA().getApplicationType() + "' does not exists");
	            

	            bpaRequest.getBPA().getOwners().forEach(owner -> {
	            	 if(!masterData.get(BPAConstants.OWNER_TYPE).
	 	                    contains(owner.getOwnerType()))
	 	                errorMap.put("INVALID OWNERTYPE", "The OwnerType '"
	 	                        + owner + "' does not exists");
	 	            
	                });
	            
	            if( !StringUtils.isEmpty(bpaRequest.getBPA().getOccupancyType()) &&
	            		!masterData.get(BPAConstants.OCCUPANCY_TYPE).
	                    contains(bpaRequest.getBPA().getOccupancyType()))
	                errorMap.put("INVALID OCCUPANCYTYPE", "The OccupancyType '"
	                        + bpaRequest.getBPA().getOccupancyType() + "' does not exists");

	            if( !StringUtils.isEmpty(bpaRequest.getBPA().getSubOccupancyType()) && 
	            		!masterData.get(BPAConstants.SUB_OCCUPANCY_TYPE).
	                    contains(bpaRequest.getBPA().getSubOccupancyType()))
	                errorMap.put("INVALID SUBOCCUPANCYTYPE", "The SubOccupancyType '"
	                        + bpaRequest.getBPA().getSubOccupancyType() + "' does not exists");

	            if(!StringUtils.isEmpty(bpaRequest.getBPA().getUsages()) && 
	            		!masterData.get(BPAConstants.USAGES).
	                    contains(bpaRequest.getBPA().getUsages()))
	                errorMap.put("INVALID USAGES", "The Usages '"
	                        + bpaRequest.getBPA().getUsages() + "' does not exists");
	            
		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}



    /**
     * Fetches all the values of particular attribute as map of field name to list
     *
     * takes all the masters from each module and adds them in to a single map
     *
     * note : if two masters from different modules have the same name then it
     *
     *  will lead to overriding of the earlier one by the latest one added to the map
     *
     * @return Map of MasterData name to the list of code in the MasterData
     *
     */
    public Map<String, List<String>> getAttributeValues(Object mdmsData) {

        List<String> modulepaths = Arrays.asList(BPAConstants.BPA_JSONPATH_CODE,
        		BPAConstants.COMMON_MASTER_JSONPATH_CODE);
        final Map<String, List<String>> mdmsResMap = new HashMap<>();
        modulepaths.forEach( modulepath -> {
            try {
                mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
                System.out.println(mdmsResMap);
            } catch (Exception e) {
                log.error("Error while fetvhing MDMS data", e);
                throw new CustomException(BPAConstants.INVALID_TENANT_ID_MDMS_KEY, BPAConstants.INVALID_TENANT_ID_MDMS_MSG);
            }
        });

        System.err.println(" the mdms response is : " + mdmsResMap);
        return mdmsResMap;
    }
	
    

    /**
     * Validates if MasterData is properly fetched for the given MasterData names
     * @param masterNames
     * @param codes
     */
    private void validateIfMasterPresent(String[] masterNames,Map<String,List<String>> codes){
        Map<String,String> errorMap = new HashMap<>();
        for(String masterName:masterNames){
            if(CollectionUtils.isEmpty(codes.get(masterName))){
                errorMap.put("MDMS DATA ERROR ","Unable to fetch "+masterName+" codes from MDMS");
            }
        }
        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

	
}
