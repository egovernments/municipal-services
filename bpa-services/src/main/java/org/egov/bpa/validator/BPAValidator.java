package org.egov.bpa.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.Unit;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

@Component
@Slf4j
public class BPAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	@Autowired
	private BPAConfiguration config;

	public void validateCreate(BPARequest bpaRequest, Object mdmsData) {
		validateDuplicateDocuments(bpaRequest);
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
		validateApplicationDocuments(bpaRequest,mdmsData);
	}

	@SuppressWarnings("static-access")
	private void validateApplicationDocuments(BPARequest request, Object mdmsData){
		Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);
		BPA bpa = request.getBPA();
//		String filterExp = "$.[?(@.applicationType=='"+bpa.getApplicationType()+"' && @.ServiceType=='"+bpa.getServiceType()+"' && @.RiskType=='"+bpa.getRiskType()+"' && @.WFState=='"+bpa.getAction()+"')].docTypes";
		
		String filterExp = "$.[?(@.applicationType=='"+bpa.getApplicationType()+"' && @.ServiceType=='"+bpa.getServiceType()+"' && @.RiskType=='"+bpa.getRiskType()+"')].docTypes";
		List<Object> docTypeMappings = JsonPath.read(masterData.get(BPAConstants.DOCUMENT_TYPE_MAPPING), filterExp);
		
		filterExp = "$.[?(@.required==true)].code";
		List<String> requiredDocTypes = JsonPath.read(docTypeMappings.get(0), filterExp);
		
		List<String> documentTypes = masterData.get(BPAConstants.DOCUMENT_TYPE);
		
		
		
		if (request.getBPA().getDocuments() != null) {
			
			bpa.getDocuments().forEach(document -> {
				
				if( !documentTypes.contains(document.getDocumentType())){
					throw new CustomException("Unkonwn Document Type ERROR",
							document.getDocumentType() + " is Unkown");
				}
			});
			
			if (requiredDocTypes.size() > 0
					&& bpa.getDocuments().size() < requiredDocTypes.size()) {
				
				throw new CustomException("Mandatory Documents missing ERROR",
						requiredDocTypes.size() + " Documents are requied ");
			} else if(requiredDocTypes.size() > 0){

				List<String> addedDocTypes= new ArrayList<String>();
				bpa.getDocuments().forEach(document -> {
					String documentNs = document.getDocumentType().split("\\.")[0];
					addedDocTypes.add(documentNs);
				});
				requiredDocTypes.forEach(docType ->{
					System.out.println(docType);
					String docType1 = docType.toString();
					if(!addedDocTypes.contains(docType1.split("\\.")[0])){
						throw new CustomException("Mandatory Documents missing ERROR",
								"Document Type "+ docType1 + " is Missing");
					}
				});
			}
		}else if (requiredDocTypes.size() > 0){
			throw new CustomException("Mandatory Documents missing ERROR",
					requiredDocTypes.size() + " Documents are requied ");
		}
		
	}
	private void validateDuplicateDocuments(BPARequest request) {
		List<String> documentFileStoreIds = new LinkedList();
		if (request.getBPA().getDocuments() != null) {
			request.getBPA()
					.getDocuments()
					.forEach(
							document -> {
								if (documentFileStoreIds.contains(document
										.getFileStore()))
									throw new CustomException(
											"DUPLICATE_DOCUMENT ERROR",
											"Same document cannot be used multiple times");
								else
									documentFileStoreIds.add(document
											.getFileStore());
							});
		}
	}

	/**
	 * Validates if the search parameters are valid
	 * 
	 * @param requestInfo
	 *            The requestInfo of the incoming request
	 * @param criteria
	 *            The BPASearch Criteria
	 */
	public void validateSearch(RequestInfo requestInfo,
			BPASearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")
				&& criteria.isEmpty())
			throw new CustomException("INVALID SEARCH",
					"Search without any paramters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")
				&& criteria.tenantIdOnly())
			throw new CustomException("INVALID SEARCH",
					"Search based only on tenantId is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException("INVALID SEARCH",
					"TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")
				&& !criteria.isEmpty() && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException("INVALID SEARCH",
					"TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")
				&& criteria.tenantIdOnly())
			throw new CustomException("INVALID SEARCH",
					"Search only on tenantId is not allowed");

		String allowedParamStr = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
			allowedParamStr = config.getAllowedCitizenSearchParameters();
		else if (requestInfo.getUserInfo().getType()
				.equalsIgnoreCase("EMPLOYEE"))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException("INVALID SEARCH", "The userType: "
					+ requestInfo.getUserInfo().getType()
					+ " does not have any search config");

		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException("INVALID SEARCH",
					"No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr
					.split(","));
			validateSearchParams(criteria, allowedParams);
		}
	}

	/**
	 * Validates if the paramters coming in search are allowed
	 * 
	 * @param criteria
	 *            BPA search criteria
	 * @param allowedParams
	 *            Allowed Params for search
	 */
	private void validateSearchParams(BPASearchCriteria criteria,
			List<String> allowedParams) {

		if (criteria.getApplicationNo() != null
				&& !allowedParams.contains("applicationNo"))
			throw new CustomException("INVALID SEARCH",
					"Search on applicationNo is not allowed");
		
		if (criteria.getEdcrNumber() != null
				&& !allowedParams.contains("edcrNumber"))
			throw new CustomException("INVALID SEARCH",
					"Search on edcrNumber is not allowed");

		if (criteria.getStatus() != null && !allowedParams.contains("status"))
			throw new CustomException("INVALID SEARCH",
					"Search on Status is not allowed");

		if (criteria.getIds() != null && !allowedParams.contains("ids"))
			throw new CustomException("INVALID SEARCH",
					"Search on ids is not allowed");

		if (criteria.getMobileNumber() != null
				&& !allowedParams.contains("mobileNumber"))
			throw new CustomException("INVALID SEARCH",
					"Search on mobileNumber is not allowed");

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException("INVALID SEARCH",
					"Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException("INVALID SEARCH",
					"Search on limit is not allowed");

	}

	public void validateUpdate(BPARequest bpaRequest, List<BPA> searchResult,
			Object mdmsData) {

		BPA bpa = bpaRequest.getBPA();

		validateAllIds(searchResult, bpa);
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
		validateBPAUnits(bpaRequest);
		validateDuplicateDocuments(bpaRequest);
		setFieldsFromSearch(bpaRequest, searchResult, mdmsData);
		validateOwnerActiveStatus(bpaRequest);

	}

	private void validateOwnerActiveStatus(BPARequest bpaRequest) {
		Map<String, String> errorMap = new HashMap<>();
		Boolean flag = false;
		for (OwnerInfo ownerInfo : bpaRequest.getBPA().getOwners()) {
			if (ownerInfo.getUserActive()) {
				flag = true;
				break;
			}
		}
		if (!flag)
			errorMap.put("INVALID OWNER",
					"All owners are inactive for application:  "
							+ bpaRequest.getBPA().getApplicationNo());
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private void setFieldsFromSearch(BPARequest bpaRequest,
			List<BPA> searchResult, Object mdmsData) {
		Map<String, BPA> idToBPAFromSearch = new HashMap<>();

		searchResult.forEach(bpa -> {
			idToBPAFromSearch.put(bpa.getId(), bpa);
		});

		bpaRequest
				.getBPA()
				.getAuditDetails()
				.setCreatedBy(
						idToBPAFromSearch.get(bpaRequest.getBPA().getId())
								.getAuditDetails().getCreatedBy());
		bpaRequest
				.getBPA()
				.getAuditDetails()
				.setCreatedTime(
						idToBPAFromSearch.get(bpaRequest.getBPA().getId())
								.getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().setStatus(
				idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getStatus());
	}

	private void validateBPAUnits(BPARequest bpaRequest) { // completed
		// TODO Auto-generated method stub
		Map<String, String> errorMap = new HashMap<>();

		BPA bpa = bpaRequest.getBPA();

		Boolean flag = false;
		List<Unit> units = bpa.getUnits();
		for (Unit unit : units) {
			if (unit.getId() != null) // && unit.getActive()
				flag = true;
			else if (unit.getId() == null)
				flag = true;
		}
		if (!flag)
			errorMap.put(
					"INVALID UPDATE",
					"All Units are inactive in the bpa: "
							+ bpa.getApplicationNo());

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	private void validateAllIds(List<BPA> searchResult, BPA bpa) {

		Map<String, BPA> idToBPAFromSearch = new HashMap<>();
		searchResult.forEach(bpas -> {
			idToBPAFromSearch.put(bpas.getId(), bpas);
		});

		Map<String, String> errorMap = new HashMap<>();
		BPA searchedBpa = idToBPAFromSearch.get(bpa.getId());

		if (!searchedBpa.getApplicationNo().equalsIgnoreCase(
				bpa.getApplicationNo()))
			errorMap.put(
					"INVALID UPDATE",
					"The application number from search: "
							+ searchedBpa.getApplicationNo()
							+ " and from update: " + bpa.getApplicationNo()
							+ " does not match");

		if (!searchedBpa.getId().equalsIgnoreCase(bpa.getId()))
			errorMap.put("INVALID UPDATE", "The id " + bpa.getId()
					+ " does not exist");

		/*if (!searchedBpa.getAddress().getId()
				.equalsIgnoreCase(bpa.getAddress().getId()))
			errorMap.put("INVALID UPDATE", "The id " + bpa.getAddress().getId()
					+ " does not exist");*/

		compareIdList(getUnitIds(searchedBpa), getUnitIds(bpa), errorMap);
		compareIdList(getOwnerIds(searchedBpa), getOwnerIds(bpa), errorMap);
		compareIdList(getOwnerDocIds(searchedBpa), getOwnerDocIds(bpa),
				errorMap);
		compareIdList(getDocumentIds(searchedBpa), getDocumentIds(bpa),
				errorMap);

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
	}

	private List<String> getDocumentIds(BPA searchedBpa) {
		List<String> applicationDocIds = new LinkedList<>();
		if (!CollectionUtils.isEmpty(searchedBpa.getDocuments())) {
			searchedBpa.getDocuments().forEach(document -> {
				applicationDocIds.add(document.getId());
			});
		}
		return applicationDocIds;
	}

	private List<String> getOwnerDocIds(BPA searchedBpa) {

		List<String> ownerDocIds = new LinkedList<>();
		if (!CollectionUtils.isEmpty(searchedBpa.getOwners())) {
			searchedBpa.getOwners().forEach(owner -> {
				if (!CollectionUtils.isEmpty(owner.getDocuments())) {
					owner.getDocuments().forEach(document -> {
						ownerDocIds.add(document.getId());
					});
				}
			});
		}
		return ownerDocIds;
	}

	private List<String> getOwnerIds(BPA searchedBpa) {

		List<String> ownerIds = new LinkedList<>();
		if (!CollectionUtils.isEmpty(searchedBpa.getOwners())) {
			searchedBpa.getOwners().forEach(owner -> {
				if (owner.getUserActive() != null)
					ownerIds.add(owner.getUuid());
			});
		}
		return ownerIds;
	}

	/**
	 * Checks if the ids are present in the searchedIds
	 * 
	 * @param searchIds
	 *            Ids got from search
	 * @param updateIds
	 *            The ids received from update Request
	 * @param errorMap
	 *            The map for collecting errors
	 */

	private void compareIdList(List<String> searchIds, Object updateIds,
			Map<String, String> errorMap) {
		if (searchIds != null)
			searchIds.forEach(searchId -> {
				if (!((List<String>) updateIds).contains(searchId))
					errorMap.put("INVALID UPDATE", "The id: " + searchIds
							+ " was not present in update request");
			});
	}

	private List<String> getUnitIds(BPA searchedBpa) {
		List<String> unitIds = new LinkedList<>();
		searchedBpa.getUnits().forEach(unit -> {
			unitIds.add(unit.getId());
		});
		return unitIds;
	}
}
