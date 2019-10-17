package org.egov.pt.calculator.validator;

import com.jayway.jsonpath.JsonPath;
import org.apache.commons.lang3.StringUtils;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.pt.calculator.repository.PTCalculatorRepository;
import org.egov.pt.calculator.service.MutationBillingSlabService;
import org.egov.pt.calculator.util.BillingSlabConstants;
import org.egov.pt.calculator.util.BillingSlabUtils;
import org.egov.pt.calculator.web.models.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MutationBillingSlabValidator {

    @Autowired
    private BillingSlabUtils billingSlabUtils;

    @Autowired
    private PTCalculatorRepository repository;

    @Autowired
    private MutationBillingSlabService mutationBillingSlabService;


    public void validateCreate(MutationBillingSlabReq mutationBillingSlabReq) {
        Map<String, String> errorMap = new HashMap<>();
        validateIfTenantIdIsUnique(mutationBillingSlabReq, errorMap);
        validateDuplicateBillingSlabs(mutationBillingSlabReq, errorMap);
        fetchAndvalidateMDMSCodes(mutationBillingSlabReq, errorMap);
        if (!CollectionUtils.isEmpty(errorMap)) {
            throw new CustomException(errorMap);
        }
    }

    public void validateUpdate(MutationBillingSlabReq mutationBillingSlabReq) {
        Map<String, String> errorMap = new HashMap<>();
        validateIfTenantIdIsUnique(mutationBillingSlabReq, errorMap);
        checkIfBillingSlabsExist(mutationBillingSlabReq, errorMap);
        fetchAndvalidateMDMSCodes(mutationBillingSlabReq, errorMap);
        if (!CollectionUtils.isEmpty(errorMap)) {
            throw new CustomException(errorMap);
        }
    }

    public void validateIfTenantIdIsUnique(MutationBillingSlabReq mutationBillingSlabReq, Map<String, String> errorMap) {
        Set<String> tenantIds = mutationBillingSlabReq.getMutationBillingSlab().parallelStream().map(MutationBillingSlab::getTenantId)
                .collect(Collectors.toSet());
        if(tenantIds.isEmpty())
            errorMap.put("EG_PT_INVALID_INPUT", "Input must have atleast one billing slab");
        else if(tenantIds.size() > 1)
            errorMap.put("EG_PT_INVALID_INPUT", "All billing slabs must belong to same tenant");

        if (!CollectionUtils.isEmpty(errorMap)) {
            throw new CustomException(errorMap);
        }
    }


    public void checkIfBillingSlabsExist(MutationBillingSlabReq mutationBillingSlabReq, Map<String, String> errorMap) {
        List<String> ids = mutationBillingSlabReq.getMutationBillingSlab().parallelStream().map(MutationBillingSlab::getId)
                .collect(Collectors.toList());
        MutationBillingSlabRes mutationBillingSlabRes = mutationBillingSlabService.searchMutationBillingSlabs(mutationBillingSlabReq.getRequestInfo(),
                BillingSlabSearchCriteria.builder().id(ids).tenantId(mutationBillingSlabReq.getMutationBillingSlab().get(0).getTenantId()).build());
        if (CollectionUtils.isEmpty(mutationBillingSlabRes.getMutationBillingSlab())) {
            errorMap.put("EG_PT_INVALID_IDS", "Following records are unavailable, IDs: "+ ids);
        } else {
            List<String> idsAvailableintheDB = mutationBillingSlabRes.getMutationBillingSlab().parallelStream().map(MutationBillingSlab::getId)
                    .collect(Collectors.toList());
            if (idsAvailableintheDB.size() != ids.size()) {
                List<String> invalidIds = new ArrayList<>();
                for (String id : ids) {
                    if (!idsAvailableintheDB.contains(id))
                        invalidIds.add(id);
                }
                errorMap.put("EG_PT_INVALID_IDS", "Following records are unavailable, IDs: " + invalidIds);
            }
        }

        if (!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
    }

    /**
     * Validates the Incoming request for duplicate Records
     *
     * @param mutationBillingSlabReq
     * @param errorMap
     */
    public void validateDuplicateBillingSlabs(MutationBillingSlabReq mutationBillingSlabReq, Map<String, String> errorMap) {

        List<MutationBillingSlab> incomingSlabs = mutationBillingSlabReq.getMutationBillingSlab();
        String tenantId = incomingSlabs.get(0).getTenantId();
        List<Integer> errorList = new ArrayList<>();

        List<MutationBillingSlab> dbSlabs = mutationBillingSlabService.searchMutationBillingSlabs(mutationBillingSlabReq.getRequestInfo(),
                BillingSlabSearchCriteria.builder().tenantId(tenantId).build()).getMutationBillingSlab();

        if (!CollectionUtils.isEmpty(dbSlabs)) {
            for (int i = 0; i < incomingSlabs.size(); i++) {
                Integer index = null;
                if (dbSlabs.contains(incomingSlabs.get(i))) {
                    log.info("Equals passed!");
                    errorList.add(i);
/*					index = dbSlabs.indexOf(incomingSlabs.get(i));
					BillingSlab dbSlab = dbSlabs.get(index);
					if(!billingSlabUtils.checkIfRangeSatisfies(dbSlab, incomingSlabs.get(i))){
						log.info("Range doesn't statisfy.");
						errorList.add(i);
					}*/
                }
            }
            if (!CollectionUtils.isEmpty(errorList))
                errorMap.put("EG_PT_BILLING_SLAB_DUPLICATE",
                        "Records in following indices are duplicate, : " + errorList);
        }
        if (!CollectionUtils.isEmpty(errorMap))
            throw new CustomException(errorMap);
    }	
    
    
    
    public void fetchAndvalidateMDMSCodes(MutationBillingSlabReq mutationBillingSlabReq, Map<String, String> errorMap) {
        StringBuilder uri = new StringBuilder();
        MdmsCriteriaReq request = billingSlabUtils.prepareRequest(uri,
                mutationBillingSlabReq.getMutationBillingSlab().get(0).getTenantId(), mutationBillingSlabReq.getRequestInfo());
        Object response = null;
        try {
            response = repository.fetchResult(uri, request);
            if (null == response) {
                log.info(BillingSlabConstants.MDMS_DATA_NOT_FOUND_MESSAGE);
                throw new CustomException();
            }
            validateMDMSCodes(mutationBillingSlabReq, errorMap, response);
        } catch (Exception e) {
            log.error(BillingSlabConstants.MDMS_DATA_NOT_FOUND_KEY, e);
            errorMap.put(BillingSlabConstants.MDMS_DATA_NOT_FOUND_KEY,
                    BillingSlabConstants.MDMS_DATA_NOT_FOUND_MESSAGE);
            return;
        }
    }



    public void validateMDMSCodes(MutationBillingSlabReq mutationBillingSlabReq, Map<String, String> errorMap, Object mdmsResponse) {

        final String allValue = BillingSlabConstants.ALL_PLACEHOLDER_BILLING_SLAB;

        List<Object> usageType = new ArrayList<>();
        List<Object> ownerShipType = new ArrayList<>();

        try {
            usageType = JsonPath.read(mdmsResponse, BillingSlabConstants.MDMS_PROPERTYTAX_JSONPATH + BillingSlabConstants.MDMS_OWNERSHIP_MASTER_NAME);
            ownerShipType = JsonPath.read(mdmsResponse, BillingSlabConstants.MDMS_PROPERTYTAX_JSONPATH + BillingSlabConstants.MDMS_SUBOWNERSHIP_MASTER_NAME);
        } catch (Exception e) {
            if (CollectionUtils.isEmpty(usageType) && CollectionUtils.isEmpty(ownerShipType)) {
                log.error("MDMS data couldn't be fetched. Skipping code validation.....", e);
                return;
            }
        }
        /*
         * usage type is not allowed to have ALL string value
         */
        for (MutationBillingSlab mutationBillingSlab : mutationBillingSlabReq.getMutationBillingSlab()) {
            if(!StringUtils.isEmpty(mutationBillingSlab.getUsageType())) {
                List<String> allowedUsageTypes = JsonPath.read(usageType, BillingSlabConstants.MDMS_CODE_JSONPATH);
                if(!allowedUsageTypes.contains(mutationBillingSlab.getUsageType()) && !(mutationBillingSlab.getUsageType().equalsIgnoreCase(allValue)))
                    errorMap.put("INVALID_OCCUPANCY_TYPE","Occupancy Type provided is invalid");
            }

            
            if(!StringUtils.isEmpty(mutationBillingSlab.getOwnerShipType())) {
                List<String> allowedOwnerShipTypes = JsonPath.read(ownerShipType,BillingSlabConstants.MDMS_CODE_JSONPATH);
                if(!allowedOwnerShipTypes.contains(mutationBillingSlab.getOwnerShipType()) && !(mutationBillingSlab.getOwnerShipType().equalsIgnoreCase(allValue)) ) {
                    errorMap.put("INVALID_OWNERSHIP_CATEGORY","Ownership category is invalid");

                }
            }
        
        }
    }
}
