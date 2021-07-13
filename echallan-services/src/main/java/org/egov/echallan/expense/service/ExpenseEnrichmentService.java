package org.egov.echallan.expense.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.expense.model.AuditDetails;
import org.egov.echallan.expense.model.Expense;
import org.egov.echallan.expense.model.Expense.StatusEnum;
import org.egov.echallan.expense.model.ExpenseRequest;
import org.egov.echallan.expense.model.SearchCriteria;
import org.egov.echallan.expense.model.UserInfo;
import org.egov.echallan.expense.repository.ExpenseRepository;
import org.egov.echallan.expense.repository.IdGenExpenseRepository;
import org.egov.echallan.expense.util.ExpenseCommonUtils;
import org.egov.echallan.expense.web.models.Idgen.IdResponse;
import org.egov.echallan.expense.web.models.user.User;
import org.egov.echallan.expense.web.models.user.UserDetailResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ExpenseEnrichmentService {

    private IdGenExpenseRepository idGenExpenseRepository;
    private ChallanConfiguration config;
    private ExpenseCommonUtils commUtils;
    private ExpenseRepository expenseRepository;
    
    @Autowired
    public ExpenseEnrichmentService(IdGenExpenseRepository idGenExpenseRepository, ChallanConfiguration config, ExpenseCommonUtils commonUtils,  
    		ExpenseRepository challanRepository) {
		this.idGenExpenseRepository = idGenExpenseRepository;
        this.config = config;
        this.commUtils = commonUtils;
        this.expenseRepository = challanRepository;
    }

    public void enrichCreateRequest(ExpenseRequest expenseRequest) {
		RequestInfo requestInfo = expenseRequest.getRequestInfo();
		String uuid = requestInfo.getUserInfo().getUuid();
		AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
		Expense expense = expenseRequest.getExpense();
		expense.setAuditDetails(auditDetails);
		expense.setId(UUID.randomUUID().toString());
		if (expense.getIsBillPaid())
			expense.setApplicationStatus(StatusEnum.PAID);
		else
			expense.setApplicationStatus(StatusEnum.ACTIVE);
		if (expense.getAddress() != null) {
			expense.getAddress().setId(UUID.randomUUID().toString());
			expense.getAddress().setTenantId(expense.getTenantId());
		}
		expense.setFilestoreid(null);
		setIdgenIds(expenseRequest);
    }

    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenExpenseRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }

    private void setIdgenIds(ExpenseRequest request) {
        RequestInfo requestInfo = request.getRequestInfo();
        String tenantId = request.getExpense().getTenantId();
        Expense expense = request.getExpense();
        String challanNo = getIdList(requestInfo, tenantId, config.getExpenseNumberIdgenName(), config.getExpenseNumberIdgenFormat(), 1).get(0);
        expense.setChallanNo(challanNo);
    }

    public void enrichSearchCriteriaWithAccountId(RequestInfo requestInfo,SearchCriteria criteria){
        if(criteria.isEmpty() && requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN")){
            criteria.setAccountId(requestInfo.getUserInfo().getUuid());
            criteria.setMobileNumber(requestInfo.getUserInfo().getUserName());
            criteria.setTenantId(requestInfo.getUserInfo().getTenantId());
        }

    }
    
    public SearchCriteria getExpenseCriteriaFromIds(List<Expense> expenses){
        SearchCriteria criteria = new SearchCriteria();
        Set<String> ids = new HashSet<>();
        Set<String> businessids = new HashSet<>();
        expenses.forEach(expense -> ids.add(expense.getId()));
        expenses.forEach(expense -> businessids.add(expense.getBusinessService()));

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

    public void enrichOwner(UserDetailResponse userDetailResponse, List<Expense> expenses){
        List<UserInfo> users = userDetailResponse.getUser();
        Map<String,User> userIdToOwnerMap = new HashMap<>();
        users.forEach(user -> userIdToOwnerMap.put(user.getUuid(),user));
        expenses.forEach(expense -> {
        	if(expense.getAccountId()==null)
                        throw new CustomException("OWNER SEARCH ERROR","The owner of the challan "+ expense.getId()+" is not coming in user search");
            else {
                   User user = userIdToOwnerMap.get(expense.getAccountId());
                   UserInfo userinfo = getUserInfo(user);
                    	
                   expense.setCitizen(userinfo);
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
    public List<Expense> enrichChallanSearch(List<Expense> expenses, SearchCriteria criteria, RequestInfo requestInfo){

       
        SearchCriteria searchCriteria = enrichChallanSearchCriteriaWithOwnerids(criteria,expenses);
       /* UserDetailResponse userDetailResponse = userService.getUser(searchCriteria,requestInfo);
        enrichOwner(userDetailResponse,challans);*/
        return expenses;
    }
    
    
    public SearchCriteria enrichChallanSearchCriteriaWithOwnerids(SearchCriteria criteria, List<Expense> expenses) {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.setTenantId(criteria.getTenantId());
        Set<String> ownerids = new HashSet<>();
        expenses.forEach(expense -> {
        	ownerids.add(expense.getAccountId());
        });
        searchCriteria.setUserIds(new ArrayList<>(ownerids));
        return searchCriteria;
    }

	public void enrichUpdateRequest(ExpenseRequest request) {
		 RequestInfo requestInfo = request.getRequestInfo();
	     String uuid = requestInfo.getUserInfo().getUuid();
	     AuditDetails auditDetails = commUtils.getAuditDetails(uuid, false);
	     Expense expense = request.getExpense();
	     expense.setAuditDetails(auditDetails);
	     String fileStoreId = expense.getFilestoreid();
	     if(fileStoreId!=null) {
	    	 expenseRepository.setInactiveFileStoreId(expense.getTenantId().split("\\.")[0], Collections.singletonList(fileStoreId));
	     }
	     expense.setFilestoreid(null);
	}

}
