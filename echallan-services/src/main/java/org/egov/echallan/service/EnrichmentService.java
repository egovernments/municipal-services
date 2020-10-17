package org.egov.echallan.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.AuditDetails;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.Challan.StatusEnum;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.model.SearchCriteria;
import org.egov.echallan.model.UserInfo;
import org.egov.echallan.repository.ChallanRepository;
import org.egov.echallan.repository.IdGenRepository;
import org.egov.echallan.repository.ServiceRequestRepository;
import org.egov.echallan.util.CommonUtils;
import org.egov.echallan.web.models.Idgen.IdResponse;
import org.egov.echallan.web.models.user.User;
import org.egov.echallan.web.models.user.UserDetailResponse;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.jayway.jsonpath.JsonPath;

import java.util.*;
import java.util.stream.Collectors;

import static org.egov.echallan.util.ChallanConstants.*;

@Service
public class EnrichmentService {

    private IdGenRepository idGenRepository;
    private ChallanConfiguration config;
    private CommonUtils commUtils;
    private UserService userService;
    private ChallanRepository challanRepository;
    private ServiceRequestRepository serviceRequestRepository;
    
    @Autowired
    public EnrichmentService(IdGenRepository idGenRepository, ChallanConfiguration config, CommonUtils commonUtils, UserService userService, 
    		ChallanRepository challanRepository,ServiceRequestRepository serviceRequestRepository) {
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.commUtils = commonUtils;
        this.userService = userService;
        this.challanRepository = challanRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public void enrichCreateRequest(ChallanRequest challanRequest) {
        RequestInfo requestInfo = challanRequest.getRequestInfo();
        String uuid = requestInfo.getUserInfo().getUuid();
        AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
        Challan challan = challanRequest.getChallan();
        challan.setAuditDetails(auditDetails);
        challan.setId(UUID.randomUUID().toString());
        challan.setApplicationStatus(StatusEnum.ACTIVE);
        if(challan.getAddress()!=null) {
        	challan.getAddress().setId(UUID.randomUUID().toString());
        	challan.getAddress().setTenantId(challan.getTenantId());
        }
        challan.setFilestoreid(null);
        if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
        challan.setAccountId(requestInfo.getUserInfo().getUuid());
        setIdgenIds(challanRequest);
        setGLCode(challanRequest);
    }

    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }

    private void setIdgenIds(ChallanRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getChallan().getTenantId();
        Challan challan = request.getChallan();
        String challanNo = getIdList(requestInfo, tenantId, config.getChallannNumberIdgenName(), config.getChallanNumberIdgenFormat(), 1).get(0);
        challan.setChallanNo(challanNo);
    }

    public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo,SearchCriteria criteria){
        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            criteria.setAccountId(requestInfo.getUserInfo().getUuid());
            criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
            criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
        }

    }
    
    public SearchCriteria getChallanCriteriaFromIds(List<Challan> challans){
        SearchCriteria criteria = new SearchCriteria();
        Set<String> ids = new HashSet<>();
        Set<String> businessids = new HashSet<>();
        challans.forEach(challan -> ids.add(challan.getId()));
        challans.forEach(challan -> businessids.add(challan.getBusinessService()));

        String businessService = String.join(",", businessids);
        criteria.setIds(new LinkedList<>(ids));
        criteria.setBusinessService(businessService);
        return criteria;
    }
    
    public void enrichSearchCriteriaWithOwnerids(SearchCriteria criteria, UserDetailResponse userDetailResponse){
        if(CollectionUtils.isEmpty(criteria.getUserIds())){
            Set<String> userIds = new HashSet<>();
            userDetailResponse.getUser().forEach(owner -> userIds.add(owner.getUuid()));
            criteria.setUserIds(new ArrayList<>(userIds));
        }
    }

    public void enrichOwner(UserDetailResponse userDetailResponse, List<Challan> challans){
        List<UserInfo> users = userDetailResponse.getUser();
        Map<String,User> userIdToOwnerMap = new HashMap<>();
        users.forEach(user -> userIdToOwnerMap.put(user.getUuid(),user));
        challans.forEach(challan -> {
        	if(challan.getAccountId()==null)
                        throw new CustomException("OWNER SEARCH ERROR","The owner of the challan "+challan.getId()+" is not coming in user search");
            else {
                   User user = userIdToOwnerMap.get(challan.getAccountId());
                   UserInfo userinfo = getUserInfo(user);
                    	
                   challan.setCitizen(userinfo);
                 }
       });

    }

    private UserInfo getUserInfo(User user) {
    	UserInfo userinfo = new UserInfo();
    	userinfo.setUuid(user.getUuid());
    	userinfo.setId(user.getId());
    	userinfo.setUserName(user.getUserName());
    	userinfo.setCreatedBy(user.getUuid());
    	userinfo.setCreatedDate(System.currentTimeMillis());
    	userinfo.setLastModifiedDate(System.currentTimeMillis());
    	userinfo.setActive(user.getActive());
    	userinfo.setTenantId(user.getTenantId());
    	userinfo.setMobileNumber(user.getMobileNumber());
    	userinfo.setName(user.getName());
    	return userinfo;
    }
    public List<Challan> enrichChallanSearch(List<Challan> challans, SearchCriteria criteria, RequestInfo requestInfo){

       
        SearchCriteria searchCriteria = enrichChallanSearchCriteriaWithOwnerids(criteria,challans);
        UserDetailResponse userDetailResponse = userService.getUser(searchCriteria,requestInfo);
        enrichOwner(userDetailResponse,challans);
        return challans;
    }
    
    
    public SearchCriteria enrichChallanSearchCriteriaWithOwnerids(SearchCriteria criteria, List<Challan> challans) {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setTenantId(criteria.getTenantId());
        Set<String> ownerids = new HashSet<>();
        challans.forEach(challan -> {
        	ownerids.add(challan.getAccountId());
        });
        searchCriteria.setUserIds(new ArrayList<>(ownerids));
        return searchCriteria;
    }

	public void enrichUpdateRequest(ChallanRequest request) {
		 RequestInfo requestInfo = request.getRequestInfo();
	     String uuid = requestInfo.getUserInfo().getUuid();
	     AuditDetails auditDetails = commUtils.getAuditDetails(uuid, false);
	     Challan challan = request.getChallan();
	     challan.setAuditDetails(auditDetails);
	     String fileStoreId = challan.getFilestoreid();
	     if(fileStoreId!=null) {
	    	 challanRepository.setInactiveFileStoreId(challan.getTenantId().split("\\.")[0], Collections.singletonList(fileStoreId));
	     }
	     challan.setFilestoreid(null);
	}
	
	private void setGLCode(ChallanRequest request) {
		RequestInfo requestInfo = request.getRequestInfo();
		Challan challan = request.getChallan();
		String tenantId = challan.getTenantId();
		ModuleDetail glCodeRequest = getGLCodeRequest(); 
		List<ModuleDetail> moduleDetails = new LinkedList<>();
		moduleDetails.add(glCodeRequest);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(moduleDetails).tenantId(tenantId)
				.build();
		MdmsCriteriaReq mdmsCriteriaReq = MdmsCriteriaReq.builder().mdmsCriteria(mdmsCriteria)
				.requestInfo(requestInfo).build();

		StringBuilder url = new StringBuilder().append(config.getMdmsHost()).append(config.getMdmsEndPoint());

		Object result = serviceRequestRepository.fetchResult(url, mdmsCriteriaReq);
		String jsonPath = GL_CODE_JSONPATH_CODE.replace("{}",challan.getBusinessService());
		List<Map<String,Object>> jsonOutput =  JsonPath.read(result, jsonPath);
		if(jsonOutput.size()!=0) {
			Map<String,Object> glCodeObj = jsonOutput.get(0);
			challan.setAdditionalDetail(glCodeObj);
		}
	}

	private ModuleDetail getGLCodeRequest() {
		List<MasterDetail> masterDetails = new ArrayList<>();
		masterDetails.add(MasterDetail.builder().name(GL_CODE_MASTER).build());
		ModuleDetail moduleDtls = ModuleDetail.builder().masterDetails(masterDetails)
				.moduleName(BILLING_SERVICE).build();
		return moduleDtls;
	}

}
