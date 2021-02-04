package org.egov.fsm.calculator.validator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.fsm.calculator.utils.CalculatorConstants;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MDMSValidator {
	private Map<String, List<String>> mdmsResMap ;
	
		// TODO Auto-generated method stub
	public void validateMdmsData(Object mdmsData) {

		this.mdmsResMap  = getAttributeValues(mdmsData);
		String[] masterArray = { CalculatorConstants.PROPERTY_TYPE };

		validateIfMasterPresent(masterArray,this.mdmsResMap);
	
		
	}
	private void validateIfMasterPresent(String[] masterNames, Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch " + masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	public Map<String, List<String>> getAttributeValues(Object mdmsData) {

		List<String> modulepaths = Arrays.asList(CalculatorConstants.FSM_JSONPATH_CODE);
		final Map<String, List<String>> mdmsResMap = new HashMap<>();
		modulepaths.forEach(modulepath -> {
			try {
				mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath));
			} catch (Exception e) {
				log.error("Error while fetvhing MDMS data", e);
				throw new CustomException(CalculatorConstants.INVALID_TENANT_ID_MDMS_KEY,
						CalculatorConstants.INVALID_TENANT_ID_MDMS_MSG);
			}
		});
		return mdmsResMap;
	}
	
	/**
	 * validate the existnance of provided property type in MDMS
	 * @param propertyType
	 * @throws CustomException
	 */
	public void validatePropertyType(String propertyType ) throws CustomException{
		
		Map<String, String> errorMap = new HashMap<>();
		
		if( !this.mdmsResMap.get(CalculatorConstants.PROPERTY_TYPE).contains(propertyType) ) {
			errorMap.put(CalculatorConstants.INVALID_PROPERTY_TYPE," Property Type is invalid");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}
	


}