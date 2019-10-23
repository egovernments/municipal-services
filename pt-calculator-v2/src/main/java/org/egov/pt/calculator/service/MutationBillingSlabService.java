package org.egov.pt.calculator.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pt.calculator.producer.Producer;
import org.egov.pt.calculator.repository.PTCalculatorDBRepository;
import org.egov.pt.calculator.util.BillingSlabUtils;
import org.egov.pt.calculator.util.Configurations;
import org.egov.pt.calculator.util.ResponseInfoFactory;
import org.egov.pt.calculator.web.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@Slf4j
public class MutationBillingSlabService {

    @Autowired
    private BillingSlabUtils billingSlabUtils;

    @Autowired
    private Producer producer;

    @Autowired
    private Configurations configurations;

    @Autowired
    private PTCalculatorDBRepository dbRepository;

    @Autowired
    private ResponseInfoFactory factory;

    @Value("${billingslab.max.toFloor}")
    private Double maxToFloor;

    @Value("${billingslab.min.fromFloor}")
    private Double minFromFloor;

    @Value("${billingslab.max.toPLotSize}")
    private Double maxToPlotSize;


    public MutationBillingSlabRes createMutationBillingSlab(MutationBillingSlabReq mutationBillingSlabReq) {
        enrichMutationBillingSlabForCreate(mutationBillingSlabReq);
        producer.push(configurations.getMutationBillingSlabSavePersisterTopic(), mutationBillingSlabReq);
        return billingSlabUtils.getMutationBillingSlabResponse(mutationBillingSlabReq);
    }


    public MutationBillingSlabRes updateMutationBillingSlab(MutationBillingSlabReq mutationBillingSlabReq) {
        enrichBillingSlabForUpdate(mutationBillingSlabReq);
        producer.push(configurations.getMutationBillingSlabUpdatePersisterTopic(), mutationBillingSlabReq);
        return billingSlabUtils.getMutationBillingSlabResponse(mutationBillingSlabReq);
    }

    public void enrichBillingSlabForUpdate(MutationBillingSlabReq mutationBillingSlabReq) {
        for(MutationBillingSlab billingSlab: mutationBillingSlabReq.getMutationBillingSlab()) {
            billingSlab.setAuditDetails(billingSlabUtils.getAuditDetails(mutationBillingSlabReq.getRequestInfo()));
//            if(null == billingSlab.getToFloor()) {
//                billingSlab.setToFloor((null == maxToFloor) ? Double.POSITIVE_INFINITY : maxToFloor);
//            }
//            if(null == billingSlab.getToPlotSize()) {
//                billingSlab.setToPlotSize((null == maxToPlotSize) ? Double.POSITIVE_INFINITY : maxToPlotSize);
//            }
//            if(null == billingSlab.getFromFloor()) {
//                billingSlab.setFromFloor((null == minFromFloor) ? Double.NEGATIVE_INFINITY : minFromFloor);
//            }
        }
    }




    public void enrichMutationBillingSlabForCreate(MutationBillingSlabReq mutationBillingSlabReq) {
        for(MutationBillingSlab mutationBillingSlab: mutationBillingSlabReq.getMutationBillingSlab()) {
            mutationBillingSlab.setId(UUID.randomUUID().toString());
            mutationBillingSlab.setAuditDetails(billingSlabUtils.getAuditDetails(mutationBillingSlabReq.getRequestInfo()));
//			if(null == billingSlab.getToFloor()) {
//				billingSlab.setToFloor((null == maxToFloor) ? Double.POSITIVE_INFINITY : maxToFloor);
//			}
//			if(null == billingSlab.getToPlotSize()) {
//				billingSlab.setToPlotSize((null == maxToPlotSize) ? Double.POSITIVE_INFINITY : maxToPlotSize);
//			}
//			if(null == billingSlab.getFromFloor()) {
//				billingSlab.setFromFloor((null == minFromFloor) ? Double.NEGATIVE_INFINITY : minFromFloor);
//			}
        }
    }

    public MutationBillingSlabRes searchMutationBillingSlabs(RequestInfo requestInfo, BillingSlabSearchCriteria billingSlabSearcCriteria) {
        List<MutationBillingSlab> mutationBillingSlabs = null;
        try {
            mutationBillingSlabs = dbRepository.searchMutationBillingSlab(billingSlabSearcCriteria);
        } catch (Exception e) {
            log.error("Exception while fetching billing slabs from db: " + e);
            mutationBillingSlabs = new ArrayList<>();
        }
        return MutationBillingSlabRes.builder().responseInfo(factory.createResponseInfoFromRequestInfo(requestInfo, true))
                .mutationBillingSlab(mutationBillingSlabs).build();
    }
}
