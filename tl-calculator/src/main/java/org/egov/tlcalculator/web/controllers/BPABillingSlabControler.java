package org.egov.tlcalculator.web.controllers;

import javax.validation.Valid;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tlcalculator.service.BPABillingSlabService;
import org.egov.tlcalculator.service.BillingslabService;
import org.egov.tlcalculator.validator.BillingslabValidator;
import org.egov.tlcalculator.web.models.BillingSlab;
import org.egov.tlcalculator.web.models.BillingSlabReq;
import org.egov.tlcalculator.web.models.BillingSlabRes;
import org.egov.tlcalculator.web.models.BillingSlabSearchCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequestMapping("/bpabillingslab")
public class BPABillingSlabControler {


    @Autowired
    private BPABillingSlabService bpaBillingSlabService;


    /**
     * Searches Billing Slabs belonging TradeLicense based on criteria
     * @param billingSlabSearchCriteria
     * @param requestInfo
     * @return
     */
    @RequestMapping(value = "/_search", method = RequestMethod.POST)
    public ResponseEntity<BillingSlab> billingslabSearchPost(@ModelAttribute @Valid BillingSlabSearchCriteria billingSlabSearchCriteria,
                                                             @Valid @RequestBody RequestInfo requestInfo) {
        BillingSlab response = bpaBillingSlabService.search(billingSlabSearchCriteria, requestInfo);
        return new ResponseEntity<BillingSlab>(response, HttpStatus.OK);
    }

}
