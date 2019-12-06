package org.egov.bpa.validator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.Unit;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class BPAValidator {

	@Autowired
	private MDMSValidator mdmsValidator;

	public void validateCreate(BPARequest bpaRequest, Object mdmsData) {
		validateDuplicateDocuments(bpaRequest);
		mdmsValidator.validateMdmsData(bpaRequest, mdmsData);
	}

	private void validateDuplicateDocuments(BPARequest request) {
		List<String> documentFileStoreIds = new LinkedList();
            if(request.getBPA().getDocuments()!=null){
            	request.getBPA().getDocuments().forEach(
                    document -> {
                        if(documentFileStoreIds.contains(document.getFileStore()))
                            throw new CustomException("DUPLICATE_DOCUMENT ERROR","Same document cannot be used multiple times");
                        else documentFileStoreIds.add(document.getFileStore());
                    }
                );
            }
	}

	

	public void validateUpdate(BPARequest bpaRequest, List<BPA> searchResult,
			Object mdmsData) {
		
		 BPA bpa = bpaRequest.getBPA();


	      validateAllIds(searchResult,bpa); 
	      mdmsValidator.validateMdmsData(bpaRequest,mdmsData);
	      validateBPAUnits(bpaRequest); 
	      validateDuplicateDocuments(bpaRequest); 
	      setFieldsFromSearch(bpaRequest,searchResult,mdmsData); 
	      validateOwnerActiveStatus(bpaRequest); 
		
	}

	private void validateOwnerActiveStatus(BPARequest bpaRequest) {
		// TODO Auto-generated method stub
		Map<String,String> errorMap = new HashMap<>();
            Boolean flag = false;
            for(OwnerInfo ownerInfo : bpaRequest.getBPA().getOwners()){
                if(ownerInfo.getUserActive()){
                    flag=true;
                    break;
                }
            }
            if(!flag)
                errorMap.put("INVALID OWNER","All owners are inactive for application:  "+bpaRequest.getBPA().getApplicationNo());
        if(!errorMap.isEmpty())
            throw new CustomException(errorMap);
	}

	private void setFieldsFromSearch(BPARequest bpaRequest, List<BPA> searchResult,
			Object mdmsData) {
		 Map<String,BPA> idToBPAFromSearch = new HashMap<>();
		 
		 searchResult.forEach(bpa -> {
	            idToBPAFromSearch.put(bpa.getId(),bpa);
	        });
		 
		 bpaRequest.getBPA().getAuditDetails().setCreatedBy(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedBy());
         bpaRequest.getBPA().getAuditDetails().setCreatedTime(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getAuditDetails().getCreatedTime());
         bpaRequest.getBPA().setStatus(idToBPAFromSearch.get(bpaRequest.getBPA().getId()).getStatus());
	}

	private void validateBPAUnits(BPARequest bpaRequest) { //completed
		// TODO Auto-generated method stub
		 Map<String,String> errorMap = new HashMap<>();
		 
		 BPA bpa = bpaRequest.getBPA();
	        
	            Boolean flag = false;
	            List<Unit> units = bpa.getUnits();
	            for(Unit unit : units) {
	                if(unit.getId()!=null) // && unit.getActive()
	                    flag = true;
	                else if(unit.getId()==null)
	                    flag = true;
	            }
	            if(!flag)
	                errorMap.put("INVALID UPDATE","All Units are inactive in the bpa: "+bpa.getApplicationNo());
	        

	        if(!errorMap.isEmpty())
	            throw new CustomException(errorMap);
	}

	private void validateAllIds(List<BPA> searchResult, BPA bpa) {

		Map<String,BPA> idToBPAFromSearch = new HashMap<>();
	        searchResult.forEach(bpas -> {
	            idToBPAFromSearch.put(bpas.getId(),bpas);
	        });

	        Map<String,String> errorMap = new HashMap<>();
	            BPA searchedBpa = idToBPAFromSearch.get(bpa.getId());

	         
	            if(!searchedBpa.getApplicationNo().equalsIgnoreCase(bpa.getApplicationNo()))
	                errorMap.put("INVALID UPDATE","The application number from search: "+searchedBpa.getApplicationNo()
	                        +" and from update: "+bpa.getApplicationNo()+" does not match");

	            if(!searchedBpa.getId().
	                    equalsIgnoreCase(bpa.getId()))
	                errorMap.put("INVALID UPDATE","The id "+bpa.getId()+" does not exist");

	            if(!searchedBpa.getAddress().getId().
	                    equalsIgnoreCase(bpa.getAddress().getId()))
	                errorMap.put("INVALID UPDATE","The id "+bpa.getAddress().getId()+" does not exist");

	            compareIdList(getUnitIds(searchedBpa),getUnitIds(bpa),errorMap);
	            compareIdList(getOwnerIds(searchedBpa),getOwnerIds(bpa),errorMap);
	            compareIdList(getOwnerDocIds(searchedBpa),getOwnerDocIds(bpa),errorMap);
	            compareIdList(getDocumentIds(searchedBpa),getDocumentIds(bpa),errorMap);

	        if(!CollectionUtils.isEmpty(errorMap))
	            throw new CustomException(errorMap);
	}


	private List<String> getDocumentIds(BPA searchedBpa) {
		 List<String> applicationDocIds = new LinkedList<>();
	        if(!CollectionUtils.isEmpty(searchedBpa.getDocuments())){
	        	searchedBpa.getDocuments().forEach(document -> {
	                applicationDocIds.add(document.getId());
	            });
	        }
	        return applicationDocIds;
	}

	private List<String> getOwnerDocIds(BPA searchedBpa) {
		// TODO Auto-generated method stub
		
		List<String> ownerDocIds = new LinkedList<>();
        if(!CollectionUtils.isEmpty(searchedBpa.getOwners())){
        	searchedBpa.getOwners().forEach(owner -> {
                if(!CollectionUtils.isEmpty(owner.getDocuments())){
                    owner.getDocuments().forEach(document -> {
                        ownerDocIds.add(document.getId());
                    });
                }
            });
        }
        return ownerDocIds;
	}

	private List<String> getOwnerIds(BPA searchedBpa) {
		// TODO Auto-generated method stub
		
		List<String> ownerIds = new LinkedList<>();
        if(!CollectionUtils.isEmpty(searchedBpa.getOwners())){
        	searchedBpa.getOwners().forEach(owner -> {
                if(owner.getUserActive()!=null)
                    ownerIds.add(owner.getUuid());
            });
        }
        return ownerIds;
	}


	/**
     * Checks if the ids are present in the searchedIds
     * @param searchIds Ids got from search
     * @param updateIds The ids received from update Request
     * @param errorMap The map for collecting errors
     */
    	
	 private void compareIdList(List<String> searchIds, Object updateIds,
			Map<String, String> errorMap) {
		// TODO Auto-generated method stub
		 if(searchIds != null)
	            searchIds.forEach(searchId -> {
	                if(!((List<String>) updateIds).contains(searchId))
	                    errorMap.put("INVALID UPDATE","The id: "+searchIds+" was not present in update request");
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
