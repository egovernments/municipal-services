package org.egov.fsm.plantmapping.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.fsm.plantmapping.util.PlantMappingConstants;
import org.egov.fsm.plantmapping.web.model.PlantMappingRequest;
import org.egov.fsm.util.FSMErrorConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MDMSPlantMapValidator {

	private Map<String, Object> mdmsResMap ;
	
	public void validateMdmsData(PlantMappingRequest request, Object mdmsData) {

		this.mdmsResMap  = getAttributeValues(mdmsData);
		String[] masterArray = { PlantMappingConstants.MDMS_FSTP_PLANT_INFO };

		validateIfMasterPresent(masterArray,this.mdmsResMap);
	
		
	}
	
	public Map<String, Object> getAttributeValues(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(PlantMappingConstants.FSM_JSONPATH_CODE);
		final Map<String, Object> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetching MDMS data", e);
				throw new CustomException(FSMErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
						FSMErrorConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});
		return mdmsResMap;
	}
	
	private void validateIfMasterPresent(String[] masterNames, Map<String, Object> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (codes.get(masterName) ==null || CollectionUtils.isEmpty((Collection<?>) codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	
	/**
	 * validate the existnance of provided ApplicationChannel  in MDMS
	 * @param propertyType
	 * @throws CustomException
	 */
	public void validateFSTPPlantInfo(String plantCode ) throws CustomException{
		
		Map<String, String> errorMap = new HashMap<>();
		
		if( !((List<String>) this.mdmsResMap.get(PlantMappingConstants.MDMS_FSTP_PLANT_INFO)).contains(plantCode) ) {
			errorMap.put(FSMErrorConstants.INVALID_APPLICATION_CHANNEL," Application Channel is invalid");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
}
