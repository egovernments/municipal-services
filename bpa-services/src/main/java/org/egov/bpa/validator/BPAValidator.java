package org.egov.bpa.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.Document;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.Unit;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BPAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	@Autowired
	private BPAConfiguration config;

	public void validateCreate(BPARequest bpaRequest, Object mdmsData) {
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
		validateApplicationDocuments(bpaRequest, mdmsData, null);
		validateUser(bpaRequest);
	}

	private void validateUser(BPARequest bpaRequest) {
		BPA bpa = bpaRequest.getBPA();
		bpa.getOwners().forEach(user -> {
			if (org.springframework.util.StringUtils.isEmpty(user.getRelationship())) {
				throw new CustomException("BPA.CREATE.USER", " Owner relation ship is mandatory " + user.toString());
			}
		});
	}

	private void validateApplicationDocuments(BPARequest request, Object mdmsData, String currentState) {
		Map<String, List<String>> masterData = mdmsValidator.getAttributeValues(mdmsData);
		BPA bpa = request.getBPA();

		if (!bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
				&& !bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_ADHOC)
				&& !bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_PAY)) {

			String filterExp = "$.[?(@.applicationType=='" + bpa.getApplicationType() + "' && @.ServiceType=='"
					+ bpa.getServiceType() + "' && @.RiskType=='" + bpa.getRiskType() + "' && @.WFState=='"
					+ currentState + "')].docTypes";

			List<Object> docTypeMappings = JsonPath.read(masterData.get(BPAConstants.DOCUMENT_TYPE_MAPPING), filterExp);

			List<Document> allDocuments = new ArrayList<Document>();
			if (bpa.getDocuments() != null) {
				allDocuments.addAll(bpa.getDocuments());
			}

			if (CollectionUtils.isEmpty(docTypeMappings)) {
				return;
			}

			filterExp = "$.[?(@.required==true)].code";
			List<String> requiredDocTypes = JsonPath.read(docTypeMappings.get(0), filterExp);

			List<String> validDocumentTypes = masterData.get(BPAConstants.DOCUMENT_TYPE);

			if (!CollectionUtils.isEmpty(allDocuments)) {

				allDocuments.forEach(document -> {

					if (!validDocumentTypes.contains(document.getDocumentType())) {
						throw new CustomException("BPA_UNKNOWN_DOCUMENTTYPE",
								document.getDocumentType() + " is Unkown");
					}
				});

				if (requiredDocTypes.size() > 0 && allDocuments.size() < requiredDocTypes.size()) {

					throw new CustomException("BPA_MDNADATORY_DOCUMENTPYE_MISSING",
							requiredDocTypes.size() + " Documents are requied ");
				} else if (requiredDocTypes.size() > 0) {

					List<String> addedDocTypes = new ArrayList<String>();
					allDocuments.forEach(document -> {

						String docType = document.getDocumentType();
						int lastIndex = docType.lastIndexOf(".");
						String documentNs = "";
						if (lastIndex > 1) {
							documentNs = docType.substring(0, lastIndex);
						} else if (lastIndex == 1) {
							throw new CustomException("BPA_INVALID_DOCUMENTTYPE",
									document.getDocumentType() + " is Invalid");
						} else {
							documentNs = docType;
						}

						addedDocTypes.add(documentNs);
					});
					requiredDocTypes.forEach(docType -> {
						String docType1 = docType.toString();
						if (!addedDocTypes.contains(docType1)) {
							throw new CustomException("BPA_MDNADATORY_DOCUMENTPYE_MISSING",
									"Document Type " + docType1 + " is Missing");
						}
					});
				}
			} else if (requiredDocTypes.size() > 0) {
				throw new CustomException("BPA_MDNADATORY_DOCUMENTPYE_MISSING",
						"Atleast " + requiredDocTypes.size() + " Documents are requied ");
			}
			bpa.setDocuments(allDocuments);
		}

	}

	private void validateDuplicateDocuments(BPARequest request) {
		if (request.getBPA().getDocuments() != null) {
			List<String> documentFileStoreIds = new LinkedList<String>();
			request.getBPA().getDocuments().forEach(document -> {
				if (documentFileStoreIds.contains(document.getFileStoreId()))
					throw new CustomException("BPA_DUPLICATE_DOCUMENT", "Same document cannot be used multiple times");
				else
					documentFileStoreIds.add(document.getFileStoreId());
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
	public void validateSearch(RequestInfo requestInfo, BPASearchCriteria criteria) {
		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && criteria.isEmpty())
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search without any paramters is not allowed");

		if (!requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.tenantIdOnly()
				&& criteria.getTenantId() == null)
			throw new CustomException(BPAConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN) && !criteria.isEmpty()
				&& !criteria.tenantIdOnly() && criteria.getTenantId() == null)
			throw new CustomException(BPAConstants.INVALID_SEARCH, "TenantId is mandatory in search");

		String allowedParamStr = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.CITIZEN))
			allowedParamStr = config.getAllowedCitizenSearchParameters();
		else if (requestInfo.getUserInfo().getType().equalsIgnoreCase(BPAConstants.EMPLOYEE))
			allowedParamStr = config.getAllowedEmployeeSearchParameters();
		else
			throw new CustomException(BPAConstants.INVALID_SEARCH,
					"The userType: " + requestInfo.getUserInfo().getType() + " does not have any search config");

		if (StringUtils.isEmpty(allowedParamStr) && !criteria.isEmpty())
			throw new CustomException(BPAConstants.INVALID_SEARCH, "No search parameters are expected");
		else {
			List<String> allowedParams = Arrays.asList(allowedParamStr.split(","));
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
	private void validateSearchParams(BPASearchCriteria criteria, List<String> allowedParams) {

		if (criteria.getApplicationNos() != null && !allowedParams.contains("applicationNo"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on applicationNo is not allowed");

		if (criteria.getEdcrNumbers() != null && !allowedParams.contains("edcrNumber"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on edcrNumber is not allowed");

		if (criteria.getStatus() != null && !allowedParams.contains("status"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on Status is not allowed");

		if (criteria.getIds() != null && !allowedParams.contains("ids"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on ids is not allowed");

		if (criteria.getMobileNumber() != null && !allowedParams.contains("mobileNumber"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on mobileNumber is not allowed");

		if (criteria.getOffset() != null && !allowedParams.contains("offset"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on offset is not allowed");

		if (criteria.getLimit() != null && !allowedParams.contains("limit"))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "Search on limit is not allowed");

		if (criteria.getFromDate() != null && (criteria.getFromDate() > new Date().getTime()))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "From date cannot be a future date");

		if (criteria.getToDate() != null && criteria.getFromDate() != null
				&& (criteria.getFromDate() > criteria.getToDate()))
			throw new CustomException(BPAConstants.INVALID_SEARCH, "To date cannot be prior to from date");
	}

	public void validateUpdate(BPARequest bpaRequest, List<BPA> searchResult, Object mdmsData, String currentState) {

		BPA bpa = bpaRequest.getBPA();
		validateApplicationDocuments(bpaRequest, mdmsData, currentState);
		validateAllIds(searchResult, bpa);
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
		validateBPAUnits(bpaRequest);
		validateDuplicateDocuments(bpaRequest);
		setFieldsFromSearch(bpaRequest, searchResult, mdmsData);

	}

	private void setFieldsFromSearch(BPARequest bpaRequest, List<BPA> searchResult, Object mdmsData) {
		Map<String, BPA> idToBPAFromSearch = new HashMap<>();

		searchResult.forEach(bpa -> {
			idToBPAFromSearch.put(bpa.getId(), bpa);
		});

		bpaRequest.getBPA().getAuditDetails()
				.setCreatedBy(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedBy());
		bpaRequest.getBPA().getAuditDetails()
				.setCreatedTime(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedTime());
		bpaRequest.getBPA().setStatus(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getStatus());
	}

	private void validateBPAUnits(BPARequest bpaRequest) {
		Map<String, String> errorMap = new HashMap<>();

		BPA bpa = bpaRequest.getBPA();

		Boolean flag = false;
		List<Unit> units = bpa.getUnits();
		if (!CollectionUtils.isEmpty(units)) {
			for (Unit unit : units) {
				if (unit.getId() != null && unit.getActive())
					flag = true;
				else if (unit.getId() == null)
					flag = true;
			}

			if (!flag) {
				errorMap.put("INVALID_UPDATE", "All Units are inactive in the bpa: " + bpa.getApplicationNo());
			}

		}
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

		if (!searchedBpa.getApplicationNo().equalsIgnoreCase(bpa.getApplicationNo()))
			errorMap.put("INVALID UPDATE", "The application number from search: " + searchedBpa.getApplicationNo()
					+ " and from update: " + bpa.getApplicationNo() + " does not match");

		if (!searchedBpa.getId().equalsIgnoreCase(bpa.getId()))
			errorMap.put("INVALID UPDATE", "The id " + bpa.getId() + " does not exist");

		if (!searchedBpa.getAddress().getId().equalsIgnoreCase(bpa.getAddress().getId()))
			errorMap.put("INVALID UPDATE", "The id " + bpa.getAddress().getId() + " does not exist");

		compareIdList(getUnitIds(searchedBpa), getUnitIds(bpa), errorMap);

		// verify the existing owner from the bpa missing, If yes then mark the
		// missing user active false.
		Boolean allowOwnerChange = (bpa.getAction() != null
				&& (bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_APPLY)
						|| bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_INITIATE)));

		List<String> searchIds = getOwnerIds(searchedBpa);
		List<String> updateIds = getOwnerIds(bpa);
		List<OwnerInfo> missingOwners = new ArrayList<OwnerInfo>();
		if (searchIds != null) {
			searchIds.forEach(searchId -> {
				if (!((List<String>) updateIds).contains(searchId))
					if (allowOwnerChange) {
						searchedBpa.getOwners().forEach(owner -> {
							if (owner.getUuid().equalsIgnoreCase(searchId)) {
								owner.setActive(false);
								missingOwners.add(owner);
							}
						});
					} else {
						errorMap.put("INVALID UPDATE", "The id: " + searchIds + " was not present in update request");
					}

			});
		}
		if (missingOwners.size() > 0) {
			List<OwnerInfo> existingOwners = bpa.getOwners();
			existingOwners.addAll(missingOwners);
			bpa.setOwners(existingOwners);
		}

		compareIdList(getOwnerDocIds(searchedBpa), getOwnerDocIds(bpa), errorMap);

		if (!CollectionUtils.isEmpty(errorMap))
			throw new CustomException(errorMap);
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
				if (owner.getUuid() != null)
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

	@SuppressWarnings("unchecked")
	private void compareIdList(List<String> searchIds, Object updateIds, Map<String, String> errorMap) {
		if (searchIds != null)
			searchIds.forEach(searchId -> {
				if (!((List<String>) updateIds).contains(searchId))
					errorMap.put(BPAConstants.INVALID_UPDATE,
							"The id: " + searchIds + " was not present in update request");
			});
	}

	private List<String> getUnitIds(BPA searchedBpa) {
		List<String> unitIds = new LinkedList<>();
		if (!CollectionUtils.isEmpty(searchedBpa.getUnits())) {
			searchedBpa.getUnits().forEach(unit -> {
				unitIds.add(unit.getId());
			});
		}

		return unitIds;
	}

	public void validateCheckList(Object mdmsData, BPARequest bpaRequest, String wfState) {
		BPA bpa = bpaRequest.getBPA();
		validateQuestions(mdmsData, bpa, wfState);
		validateDocTypes(mdmsData, bpa, wfState);
	}

	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateQuestions(Object mdmsData, BPA bpa, String wfState) {
		List<String> mdmsQns = null;

		log.info("Fetching MDMS result for the state " + wfState);

		try {
			String questionsPath = BPAConstants.QUESTIONS_MAP.replace("{1}", wfState)
					.replace("{2}", bpa.getRiskType().toString()).replace("{3}", bpa.getServiceType())
					.replace("{4}", bpa.getApplicationType());

			List<Object> mdmsQuestionsArray = (List<Object>) JsonPath.read(mdmsData, questionsPath);

			if (!CollectionUtils.isEmpty(mdmsQuestionsArray))
				mdmsQns = JsonPath.read(mdmsQuestionsArray.get(0), BPAConstants.QUESTIONS_PATH);

			log.info("MDMS questions " + mdmsQns);
			if (!CollectionUtils.isEmpty(mdmsQns)) {
				if (bpa.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) bpa.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestQns = new ArrayList<String>();
							validateDateTime((Map)checkListFromReq.get(i));
							requestCheckList.addAll(
									(List<Map>) ((Map) (checkListFromReq).get(i)).get(BPAConstants.QUESTIONS_TYPE));

							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqQn : requestCheckList) {
									requestQns.add((String) reqQn.get(BPAConstants.QUESTION_TYPE));
								}
							}

							log.info("Request questions " + requestQns);

							if (!CollectionUtils.isEmpty(requestQns)) {
								if (requestQns.size() < mdmsQns.size())
									throw new CustomException("BPA_UNKNOWN_QUESTIONS",
											"Please answer all the questions " + StringUtils.join(mdmsQns, ","));
								else {
									List<String> pendingQns = new ArrayList<String>();
									for (String qn : mdmsQns) {
										if (!requestQns.contains(qn)) {
											pendingQns.add(qn);
										}
									}
									if (pendingQns.size() > 0) {
										throw new CustomException("BPA_UNKNOWN_QUESTIONS",
												"Please answer " + StringUtils.join(pendingQns, ","));
									}
								}
							} else {
								throw new CustomException("BPA_UNKNOWN_QUESTIONS",
										"Please answer the required questions");
							}
						}
					} else {
						throw new CustomException("BPA_UNKNOWN_QUESTIONS", "Please answer the required questions");
					}
				} else {
					throw new CustomException("BPA_UNKNOWN_QUESTIONS", "Please answer the required questions");
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Questions" + ex.getMessage());
		}
	}

	@SuppressWarnings(value = { "unchecked", "rawtypes" })
	private void validateDocTypes(Object mdmsData, BPA bpa, String wfState) {
		List<String> mdmsDocs = null;

		log.info("Fetching MDMS result for the state " + wfState);

		try {
			String docTypesPath = BPAConstants.DOCTYPES_MAP.replace("{1}", wfState)
					.replace("{2}", bpa.getRiskType().toString()).replace("{3}", bpa.getServiceType())
					.replace("{4}", bpa.getApplicationType());

			List<Object> docTypesArray = (List<Object>) JsonPath.read(mdmsData, docTypesPath);

			if (!CollectionUtils.isEmpty(docTypesArray))
				mdmsDocs = JsonPath.read(docTypesArray.get(0), BPAConstants.DOCTYPESS_PATH);

			log.info("MDMS DocTypes " + mdmsDocs);
			if (!CollectionUtils.isEmpty(mdmsDocs)) {
				if (bpa.getAdditionalDetails() != null) {
					List checkListFromReq = (List) ((Map) bpa.getAdditionalDetails()).get(wfState.toLowerCase());
					if (!CollectionUtils.isEmpty(checkListFromReq)) {
						for (int i = 0; i < checkListFromReq.size(); i++) {
							List<Map> requestCheckList = new ArrayList<Map>();
							List<String> requestDocs = new ArrayList<String>();
							requestCheckList
									.addAll((List<Map>) ((Map) (checkListFromReq).get(i)).get(BPAConstants.DOCS));
							if (!CollectionUtils.isEmpty(requestCheckList)) {
								for (Map reqDoc : requestCheckList) {
									String fileStoreId = ((String) reqDoc.get(BPAConstants.FILESTOREID));
									if (!StringUtils.isEmpty(fileStoreId)) {
										String docType = (String) reqDoc.get(BPAConstants.CODE);
										int lastIndex = docType.lastIndexOf(".");
										String documentNs = "";
										if (lastIndex > 1) {
											documentNs = docType.substring(0, lastIndex);
										} else if (lastIndex == 1) {
											throw new CustomException("BPA_INVALID_DOCUMENTTYPE",
													(String) reqDoc.get(BPAConstants.CODE) + " is Invalid");
										} else {
											documentNs = docType;
										}
										requestDocs.add(documentNs);
									} else {
										throw new CustomException("BPA_UNKNOWN_DOCS",
												"fileStoreId is not exists for the documents");
									}
								}
							}

							log.info("Request Docs " + requestDocs);

							if (!CollectionUtils.isEmpty(requestDocs)) {
								if (requestDocs.size() < mdmsDocs.size())
									throw new CustomException("BPA_UNKNOWN_DOCS",
											"Please upload all the required docs " + StringUtils.join(mdmsDocs, ","));
								else {
									List<String> pendingDocs = new ArrayList<String>();
									for (String doc : mdmsDocs) {
										if (!requestDocs.contains(doc)) {
											pendingDocs.add(doc);
										}
									}
									if (pendingDocs.size() > 0) {
										throw new CustomException("BPA_UNKNOWN_DOCS",
												"Please upload " + StringUtils.join(pendingDocs, ","));
									}
								}
							} else {
								throw new CustomException("BPA_UNKNOWN_DOCS", "Please upload required Documents");
							}
						}
					} else {
						throw new CustomException("BPA_UNKNOWN_DOCS", "Please upload required Documents");
					}
				} else {
					throw new CustomException("BPA_UNKNOWN_DOCS", "Please upload required Documents");
				}
			}
		} catch (PathNotFoundException ex) {
			log.error("Exception occured while validating the Checklist Documents" + ex.getMessage());
		}
	}
	
	private void validateDateTime(@SuppressWarnings("rawtypes") Map checkListFromRequest) {

		if (checkListFromRequest.get(BPAConstants.INSPECTION_DATE) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(BPAConstants.INSPECTION_DATE).toString())) {
			throw new CustomException("BPA_UNKNOWN_DATE", "Please mention the inspection date");
		} else if (checkListFromRequest.get(BPAConstants.INSPECTION_TIME) == null
				|| StringUtils.isEmpty(checkListFromRequest.get(BPAConstants.INSPECTION_TIME).toString())) {
			throw new CustomException("BPA_UNKNOWN_TIME", "Please mention the inspection time");
		}
	}
}
