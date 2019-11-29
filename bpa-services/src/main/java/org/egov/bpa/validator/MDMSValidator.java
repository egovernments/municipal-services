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

import com.jayway.jsonpath.JsonPath;

@Component
@Slf4j
public class MDMSValidator {

	/**
	 * method to validate the mdms data in the request
	 *
	 * @param licenseRequest
	 */
	public void validateMdmsData(BPARequest request, Object mdmsData) {

		Map<String, String> errorMap = new HashMap<>();

		// Map<String, List<String>> masterData = getAttributeValues(mdmsData);

		/*
		 * String[] masterArray = { BPAConstants.APPLICATION_TYPE,
		 * BPAConstants.OWNER_TYPE, BPAConstants.OWNERSHIP_CATEGORY,
		 * BPAConstants.DOCUMENT_TYPE };
		 * 
		 * validateIfMasterPresent(masterArray, masterData);
		 * 
		 * if (!masterData.get(BPAConstants.OWNERSHIP_CATEGORY).contains(
		 * request.getBPA().getOwnershipCategory()))
		 * errorMap.put("INVALID OWNERSHIPCATEGORY", "The OwnerShipCategory '" +
		 * request.getBPA().getOwnershipCategory() + "' does not exists");
		 * 
		 * if (!masterData.get(BPAConstants.DOCUMENT_TYPE).contains(
		 * request.getBPA().getDocuments() != null)) {
		 * 
		 * request.getBPA() .getDocuments() .forEach( document -> { if
		 * (document.getDocumentType() == null) { errorMap.put(
		 * "INVALID DOCUMENTTYPE", "The Document Type '" + document
		 * .getDocumentType() + "' does not exists"); }
		 * 
		 * }); }
		 * 
		 * if (!masterData.get(BPAConstants.OWNER_TYPE).contains(
		 * request.getBPA().getDocuments() != null)) {
		 * 
		 * request.getBPA() .getOwners() .forEach( owner -> { if
		 * (owner.getOwnerType() == null) { errorMap.put( "INVALID OWNERTYPE",
		 * "The Owner Type '" + owner.getOwnerType() + "' does not exists"); }
		 * 
		 * });
		 * 
		 * }
		 */

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}

	/**
	 * Validates if MasterData is properly fetched for the given MasterData
	 * names
	 * 
	 * @param masterNames
	 * @param codes
	 */
	private void validateIfMasterPresent(String[] masterNames,
			Map<String, List<String>> codes) {
		Map<String, String> errorMap = new HashMap<>();
		for (String masterName : masterNames) {
			if (CollectionUtils.isEmpty(codes.get(masterName))) {
				errorMap.put("MDMS DATA ERROR ", "Unable to fetch "
						+ masterName + " codes from MDMS");
			}
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Fetches all the values of particular attribute as map of field name to
	 * list
	 *
	 * takes all the masters from each module and adds them in to a single map
	 *
	 * note : if two masters from different modules have the same name then it
	 *
	 * will lead to overriding of the earlier one by the latest one added to the
	 * map
	 *
	 * @return Map of MasterData name to the list of code in the MasterData
	 *
	 */
	/*
	 * private Map<String, List<String>> getAttributeValues(Object mdmsData) {
	 * 
	 * List<String> modulepaths = Arrays.asList( BPAConstants.BPA_JSONPATH_CODE,
	 * BPAConstants.COMMON_MASTER_JSONPATH_CODE);
	 * 
	 * final Map<String, List<String>> mdmsResMap = new HashMap<>();
	 * modulepaths.forEach(modulepath -> { try {
	 * mdmsResMap.putAll(JsonPath.read(mdmsData, modulepath)); } catch
	 * (Exception e) { log.error("Error while fetvhing MDMS data", e); throw new
	 * CustomException( BPAConstants.INVALID_TENANT_ID_MDMS_KEY,
	 * BPAConstants.INVALID_TENANT_ID_MDMS_MSG); } });
	 * 
	 * System.err.println(" the mdms response is : " + mdmsResMap); return
	 * mdmsResMap; }
	 */
}
