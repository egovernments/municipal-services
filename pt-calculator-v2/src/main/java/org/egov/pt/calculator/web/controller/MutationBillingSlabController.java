package org.egov.pt.calculator.web.controller;


import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.service.MutationBillingSlabService;
import org.egov.pt.calculator.validator.MutationBillingSlabValidator;
import org.egov.pt.calculator.web.models.MutationBillingSlabReq;
import org.egov.pt.calculator.web.models.MutationBillingSlabRes;
import org.egov.pt.calculator.web.models.BillingSlabSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping
@Slf4j
public class MutationBillingSlabController {

    @Autowired
    private MutationBillingSlabService service;

    @Autowired
    private MutationBillingSlabValidator mutationBillingSlabValidator;

    /**
     * enpoint to create billing-slabs
     *
     * @param mutationBillingSlabReq 
     * @author Shreya
     */

    @PostMapping("_create")
    @ResponseBody
    private ResponseEntity<?> create(@RequestBody @Valid MutationBillingSlabReq mutationBillingSlabReq) {

        long startTime = System.currentTimeMillis();
        mutationBillingSlabValidator.validateCreate(mutationBillingSlabReq);
        MutationBillingSlabRes mutationBillingSlabRes = service.createMutationBillingSlab(mutationBillingSlabReq);
        long endTime = System.currentTimeMillis();
        log.debug(" the time taken for create in ms: {}", endTime - startTime);
        return new ResponseEntity<>(mutationBillingSlabRes, HttpStatus.CREATED);
    }

    /**
     * enpoint to update billing-slabs
     *
     * @param mutationBillingSlabReq
     * @author Shreya
     */
    @PostMapping("_update")
    @ResponseBody
    private ResponseEntity<?> update(@RequestBody @Valid MutationBillingSlabReq mutationBillingSlabReq) {

        long startTime = System.currentTimeMillis();
        mutationBillingSlabValidator.validateUpdate(mutationBillingSlabReq);
        MutationBillingSlabRes mutationBillingSlabRes = service.updateMutationBillingSlab(mutationBillingSlabReq);
        long endTime = System.currentTimeMillis();
        log.debug(" the time taken for create in ms: {}", endTime - startTime);
        return new ResponseEntity<>(mutationBillingSlabRes, HttpStatus.CREATED);
    }

    /**
     * enpoint to search billing-slabs
     *
     * @param requestInfo
     * @param billingSlabSearcCriteria 
     * @author Shreya
     */
    @PostMapping("_search")
    @ResponseBody
    private ResponseEntity<?> search(@RequestBody @Valid RequestInfo requestInfo,
                                     @ModelAttribute @Valid BillingSlabSearchCriteria billingSlabSearcCriteria) {
        long startTime = System.currentTimeMillis();
        MutationBillingSlabRes mutationBillingSlabRes = service.searchMutationBillingSlabs(requestInfo, billingSlabSearcCriteria);
        long endTime = System.currentTimeMillis();
        log.debug(" the time taken for create in ms: {}", endTime - startTime);
        return new ResponseEntity<>(mutationBillingSlabRes, HttpStatus.OK);
    }


}
