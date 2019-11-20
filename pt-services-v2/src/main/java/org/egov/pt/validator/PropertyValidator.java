package org.egov.pt.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.pt.config.PropertyConfiguration;
import org.egov.pt.models.Institution;
import org.egov.pt.models.OwnerInfo;
import org.egov.pt.models.Property;
import org.egov.pt.models.PropertyCriteria;
import org.egov.pt.repository.PropertyRepository;
import org.egov.pt.repository.ServiceRequestRepository;
import org.egov.pt.util.ErrorConstants;
import  org.egov.pt.util.PTConstants;
import org.egov.pt.util.PropertyUtil;
import org.egov.pt.web.contracts.PropertyRequest;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PropertyValidator {


    @Autowired
    private PropertyUtil propertyUtil;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndpoint;



    /**
     * Validate the masterData and ctizenInfo of the given propertyRequest
     * @param request PropertyRequest for create
     */
	public void validateCreateRequest(PropertyRequest request) {

		Map<String, String> errorMap = new HashMap<>();

		validateMasterData(request, errorMap);
		validateMobileNumber(request, errorMap);

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

    /**
     * Validates the masterData,CitizenInfo and the authorization of the assessee for update
     * @param request PropertyRequest for update
     */
    public void validateUpdateRequest(PropertyRequest request){ 
    	
    	Map<String, String> errorMap = new HashMap<>();
    	
        validateIds(request, errorMap);
        validateMobileNumber(request, errorMap);
        
        PropertyCriteria criteria = getPropertyCriteriaForSearch(request);
        List<Property> propertiesFromSearchResponse = propertyRepository.getProperties(criteria);
        boolean ifPropertyExists=PropertyExists(propertiesFromSearchResponse);
        if(!ifPropertyExists)
        {
        	throw new CustomException("PROPERTY NOT FOUND","The property to be updated does not exist");
        	}
        propertyUtil.addAddressIds(propertiesFromSearchResponse, request.getProperty());
        
        validateMasterData(request, errorMap);
        if(request.getRequestInfo().getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
            validateAssessees(request, errorMap);
        
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
    }

    /**
     * Validates if the fields in PropertyRequest are present in the MDMS master Data
     *
     * @param request PropertyRequest received for creating or update
     *
     */
    private void validateMasterData(PropertyRequest request,  Map<String,String> errorMap) {
    	
        Property property = request.getProperty();
        String tenantId = property.getTenantId();

        String[] masterNames = {PTConstants.MDMS_PT_CONSTRUCTIONSUBTYPE, PTConstants.MDMS_PT_CONSTRUCTIONTYPE, PTConstants.MDMS_PT_OCCUPANCYTYPE,
                PTConstants.MDMS_PT_PROPERTYTYPE,PTConstants.MDMS_PT_PROPERTYSUBTYPE,PTConstants.MDMS_PT_OWNERSHIP,PTConstants.MDMS_PT_SUBOWNERSHIP,
                PTConstants.MDMS_PT_USAGEMAJOR,PTConstants.MDMS_PT_USAGEMINOR,PTConstants.MDMS_PT_USAGESUBMINOR,PTConstants.MDMS_PT_USAGEDETAIL,
                PTConstants.MDMS_PT_OWNERTYPE};
        
        List<String> names = new ArrayList<>(Arrays.asList(masterNames));

        validateInstitution(property, errorMap);
        Map<String,List<String>> codes = getAttributeValues(tenantId,PTConstants.MDMS_PT_MOD_NAME,names,"$.*.code",PTConstants.JSONPATH_CODES,request.getRequestInfo());
        validateMDMSData(masterNames,codes);
        validateCodes(property,codes,errorMap);

        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }

    /**
     *Fetches all the values of particular attribute as map of fieldname to list
     *
     * @param tenantId tenantId of properties in PropertyRequest
     * @param names List of String containing the names of all masterdata whose code has to be extracted
     * @param requestInfo RequestInfo of the received PropertyRequest
     * @return Map of MasterData name to the list of code in the MasterData
     *
     */
    private Map<String,List<String>> getAttributeValues(String tenantId, String moduleName, List<String> names, String filter,String jsonpath, RequestInfo requestInfo){
        StringBuilder uri = new StringBuilder(mdmsHost).append(mdmsEndpoint);
        MdmsCriteriaReq criteriaReq = propertyUtil.prepareMdMsRequest(tenantId,moduleName,names,filter,requestInfo);
        try {
            Object result = serviceRequestRepository.fetchResult(uri, criteriaReq);
            return JsonPath.read(result,jsonpath);
        } catch (Exception e) {
            log.error("Error while fetvhing MDMS data",e);
            throw new CustomException(ErrorConstants.INVALID_TENANT_ID_MDMS_KEY,
                    ErrorConstants.INVALID_TENANT_ID_MDMS_MSG);
        }
    }

    /**
     *Checks if the codes of all fields are in the list of codes obtain from master data
     *
     * @param properties List of properties from PropertyRequest which are to validated
     * @param codes Map of MasterData name to List of codes in that MasterData
     * @param errorMap Map to fill all errors caught to send as custom Exception
     * @return Error map containing error if existed
     *
     */
    private static Map<String,String> validateCodes(Property property, Map<String,List<String>> codes, Map<String,String> errorMap){
    	

                if(!codes.get(PTConstants.MDMS_PT_PROPERTYTYPE).contains(property.getPropertyType()) && property.getPropertyType()!=null){
                    errorMap.put("Invalid PROPERTYTYPE","The PropertyType '"+property.getPropertyType()+"' does not exists");
                }

//                if(!codes.get(PTConstants.MDMS_PT_SUBOWNERSHIP).contains(property.getSubOwnershipCategory()) && property.getSubOwnershipCategory()!=null){
//                    errorMap.put("Invalid SUBOWNERSHIPCATEGORY","The SubOwnershipCategory '"+property.getSubOwnershipCategory()+"' does not exists");
//                }

                if(!codes.get(PTConstants.MDMS_PT_OWNERSHIP).contains(property.getOwnershipCategory()) && property.getOwnershipCategory()!=null){
                    errorMap.put("Invalid OWNERSHIPCATEGORY","The OwnershipCategory '"+ property.getOwnershipCategory()+"' does not exists");
                }
                
//                if(!codes.get(PTConstants.MDMS_PT_PROPERTYSUBTYPE).contains(property.getPropertySubType()) && property.getPropertySubType()!=null){
//                    errorMap.put(ErrorConstants.INVALID_PROPERTYSUBTYPE,"The PropertySubType '"+property.getPropertySubType()+"' does not exists");
//                }

//                if(!codes.get(PTConstants.MDMS_PT_USAGEMAJOR).contains(property.getUsageCategoryMajor()) && property.getUsageCategoryMajor()!=null){
//                    errorMap.put("INVALID USAGECATEGORYMAJOR","The UsageCategoryMajor '"+property.getUsageCategoryMajor()+"' at Property level does not exists");
//                }

//                if(!CollectionUtils.isEmpty(propertyDetail.getUnits()))
//                    propertyDetail.getUnits().forEach(unit ->{
//                        if(!codes.get(PTConstants.MDMS_PT_USAGEMAJOR).contains(unit.getUsageCategoryMajor()) && unit.getUsageCategoryMajor()!=null){
//                            errorMap.put("INVALID USAGECATEGORYMAJOR","The UsageCategoryMajor '"+unit.getUsageCategoryMajor()+"' at unit level does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_USAGEMINOR).contains(unit.getUsageCategoryMinor()) && unit.getUsageCategoryMinor()!=null){
//                            errorMap.put("INVALID USAGECATEGORYMINOR","The UsageCategoryMinor '"+unit.getUsageCategoryMinor()+"' does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_USAGESUBMINOR).contains(unit.getUsageCategorySubMinor()) && unit.getUsageCategorySubMinor()!=null){
//                            errorMap.put("INVALID USAGECATEGORYSUBMINOR","The UsageCategorySubMinor '"+unit.getUsageCategorySubMinor()+"' does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_USAGEDETAIL).contains(unit.getUsageCategoryDetail()) && unit.getUsageCategoryDetail()!=null){
//                            errorMap.put("INVALID USAGECATEGORYDETAIL","The UsageCategoryDetail "+unit.getUsageCategoryDetail()+" does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_CONSTRUCTIONTYPE).contains(unit.getConstructionType()) && unit.getConstructionType()!=null){
//                            errorMap.put("INVALID CONSTRUCTIONTYPE","The ConstructionType '"+unit.getConstructionType()+"' does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_CONSTRUCTIONSUBTYPE).contains(unit.getConstructionSubType()) && unit.getConstructionSubType()!=null){
//                            errorMap.put("INVALID CONSTRUCTIONSUBTYPE","The ConstructionSubType '"+unit.getConstructionSubType()+"' does not exists");
//                        }
//
//                        if(!codes.get(PTConstants.MDMS_PT_OCCUPANCYTYPE).contains(unit.getOccupancyType()) && unit.getOccupancyType()!=null){
//                            errorMap.put("INVALID OCCUPANCYTYPE","The OccupancyType '"+unit.getOccupancyType()+"' does not exists");
//                        }
//
//                        if("RENTED".equalsIgnoreCase(unit.getOccupancyType())){
//                            if(unit.getArv()==null || unit.getArv().compareTo(new BigDecimal(0))!=1)
//                                errorMap.put("INVALID ARV","Total Annual Rent should be greater than zero ");
//                        }
//                    });

                property.getOwners().forEach(owner ->{
                	
                    if(!codes.get(PTConstants.MDMS_PT_OWNERTYPE).contains(owner.getOwnerType()) && owner.getOwnerType()!=null){
                    	
                        errorMap.put("INVALID OWNERTYPE","The OwnerType '"+owner.getOwnerType()+"' does not exists");
                    }
                });

        return errorMap;

    }


    /**
     * Validates if MasterData is properly fetched for the given MasterData names
     * @param masterNames
     * @param codes
     */
    private void validateMDMSData(String[] masterNames,Map<String,List<String>> codes){
    	
        Map<String,String> errorMap = new HashMap<>();
        for(String masterName:masterNames){
            if(CollectionUtils.isEmpty(codes.get(masterName))){
                errorMap.put("MDMS DATA ERROR ","Unable to fetch "+masterName+" codes from MDMS");
            }
        }
        if (!errorMap.isEmpty())
            throw new CustomException(errorMap);
    }


	private void validateIds(PropertyRequest request, Map<String, String> errorMap) {

		Property property = request.getProperty();

		if (property.getPropertyId() == null)
			errorMap.put("INVALID PROPERTY", "Property cannot be updated without propertyId");

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

    /**
     * Returns PropertyCriteria to search for properties in database with ids set from properties in request
     *
     * @param request PropertyRequest received for update
     * @return PropertyCriteria containg ids of all properties and all its childrens
     */
    public PropertyCriteria getPropertyCriteriaForSearch(PropertyRequest request) {

        Property property=request.getProperty();
        
        PropertyCriteria propertyCriteria = new PropertyCriteria();  
        propertyCriteria.setTenantId(property.getTenantId());
        
        Set<String> ids = new HashSet<>();
        ids.add(property.getPropertyId());
        propertyCriteria.setIds(ids);
        
		if (!CollectionUtils.isEmpty(ids)) {

			if (!CollectionUtils.isEmpty(property.getOwners()))
				propertyCriteria.setOwnerIds(property.getOwners().stream().filter(owner -> owner.getUuid() != null)
						.map(OwnerInfo::getUuid).collect(Collectors.toSet()));

			if (!CollectionUtils.isEmpty(property.getDocuments()))
				property.getDocuments().stream().filter(doc -> doc.getId() != null).collect(Collectors.toSet());
		}

        return propertyCriteria;
    }

    /**
     * Checks if the property ids in search response are same as in request
     * @param request PropertyRequest received for update
     * @param responseProperties List of properties received from property Search
     * @return
     */
	public boolean PropertyExists(List<Property> responseProperties) {
		return (!CollectionUtils.isEmpty(responseProperties) && responseProperties.size() == 1);
	}

    /**
     * Validates if institution Object has null InstitutionType
     * @param request PropertyRequest which is to be validated
     * @param errorMap ErrorMap to catch and to throw error using CustomException
     */
    private void validateInstitution(Property property, Map<String,String> errorMap){
    	
		log.debug("contains check: " + property.getOwnershipCategory().contains("INSTITUTIONAL"));
		
		List<Institution> institutions = property.getInstitution();
		Boolean isOwnerCategoryInstitution = property.getOwnershipCategory().contains("INSTITUTIONAL");
		
		if (!property.getOwnershipCategory().contains("INSTITUTIONAL")
				&& !CollectionUtils.isEmpty(property.getInstitution())) {

			errorMap.put("INVALID INSTITUTION OBJECT",
					"The institution object should be null. OwnershipCategory does not contain Institutional");
			return;
		}

		institutions.forEach(institution -> {
			if (institution != null && isOwnerCategoryInstitution) {

				if (institution.getType() == null)
					errorMap.put(" INVALID INSTITUTION OBJECT ", "The institutionType cannot be null ");
				if (institution.getName() == null)
					errorMap.put("INVALID INSTITUTION OBJECT", "Institution name cannot be null");
				if (institution.getDesignation() == null)
					errorMap.put("INVALID INSTITUTION OBJECT", "Designation cannot be null");
			}
		});
	}

    /**
     * Validates the UserInfo of the the PropertyRequest. Update is allowed only for the user who created the property
     * @param request PropertyRequest received for update
     */
	private void validateAssessees(PropertyRequest request, Map<String, String> errorMap) {

		String uuid = request.getRequestInfo().getUserInfo().getUuid();
		Property property = request.getProperty();

		Set<String> owners = property.getOwners().stream().map(OwnerInfo::getUuid).collect(Collectors.toSet());

		if (!owners.contains(uuid)) {
			errorMap.put("UPDATE AUTHORIZATION FAILURE",
					"Not Authorized to assess property with propertyId " + property.getPropertyId());
		}
		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}


	/**
	 * Validates the mobileNumber of owners
	 * 
	 * @param request The propertyRequest received for create or update
	 */
	private void validateMobileNumber(PropertyRequest request, Map<String, String> errorMap) {

		Property property = request.getProperty();
		List<OwnerInfo> owners = property.getOwners();

		if (!property.getOwnershipCategory().contains("INSTITUTIONAL")) {

			owners.forEach(owner -> {
				if (!isMobileNumberValid(owner.getMobileNumber()))
					errorMap.put("INVALID OWNER", "MobileNumber is not valid for user : " + owner.getName());
			});
		} else {
			owners.forEach(owner -> {
				if (owner.getAltContactNumber() == null)
					errorMap.put("INVALID OWNER",
							"TelephoneNumber cannot be null for institution : " + owner.getName());
			});
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
	}

	/**
	 * Validator for search criteria
	 * 
	 * @param propertyCriteria
	 * @param requestInfo
	 */
    public void validatePropertyCriteria(PropertyCriteria propertyCriteria,RequestInfo requestInfo) {
    	
		List<String> allowedParams = null;

		if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
			allowedParams = Arrays.asList(propertyConfiguration.getCitizenSearchParams().split(","));
		else
			allowedParams = Arrays.asList(propertyConfiguration.getEmployeeSearchParams().split(","));

		if (propertyCriteria.getName() != null && !allowedParams.contains("name"))
			throw new CustomException("INVALID SEARCH", "Search based on name is not available");

        if(propertyCriteria.getMobileNumber()!=null && !allowedParams.contains("mobileNumber"))
            throw new CustomException("INVALID SEARCH","Search based on mobileNumber is not available");

        if(!CollectionUtils.isEmpty(propertyCriteria.getIds()) && !allowedParams.contains("ids"))
            throw new CustomException("INVALID SEARCH","Search based on ids is not available");

        if(!CollectionUtils.isEmpty(propertyCriteria.getOldpropertyids()) && !allowedParams.contains("oldpropertyids"))
            throw new CustomException("INVALID SEARCH","Search based on oldPropertyId is not available");

//        if(!CollectionUtils.isEmpty(propertyCriteria.getOwnerids()) && !allowedParams.contains("ownerids"))
//            throw new CustomException("INVALID SEARCH","Search based on ownerId is not available");


        // Search Based only on tenantId is not allowed
		Boolean emptySearch = (propertyCriteria.getName() == null && propertyCriteria.getMobileNumber() == null
				&& CollectionUtils.isEmpty(propertyCriteria.getIds())
				&& CollectionUtils.isEmpty(propertyCriteria.getOldpropertyids()));

        if(emptySearch)
            throw new CustomException("INVALID SEARCH","Search is not allowed on tenantId alone");
    }

	/**
	 * Validates if the mobileNumber is 10 digit and starts with 5 or greater
	 * 
	 * @param mobileNumber The mobileNumber to be validated
	 * @return True if valid mobileNumber else false
	 */
	private Boolean isMobileNumberValid(String mobileNumber) {

		if (mobileNumber == null)
			return false;
		else if (mobileNumber.length() != 10)
			return false;
		else if (Character.getNumericValue(mobileNumber.charAt(0)) < 5)
			return false;
		else
			return true;
	}







}
