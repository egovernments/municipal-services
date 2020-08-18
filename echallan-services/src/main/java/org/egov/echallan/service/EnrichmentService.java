package org.egov.echallan.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.echallan.config.ChallanConfiguration;
import org.egov.echallan.model.AuditDetails;
import org.egov.echallan.model.Challan;
import org.egov.echallan.model.ChallanRequest;
import org.egov.echallan.repository.IdGenRepository;
import org.egov.echallan.util.CommonUtils;
import org.egov.echallan.web.models.Idgen.IdResponse;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class EnrichmentService {

    private IdGenRepository idGenRepository;
    private ChallanConfiguration config;
    private CommonUtils commUtils;

    @Autowired
    public EnrichmentService(IdGenRepository idGenRepository, ChallanConfiguration config, CommonUtils commonUtils) {
        this.idGenRepository = idGenRepository;
        this.config = config;
        this.commUtils = commonUtils;
    }

    public void enrichCreateRequest(ChallanRequest challanRequest) {
    	System.out.println("challanRequest---"+challanRequest.toString());
        RequestInfo requestInfo = challanRequest.getRequestInfo();
        System.out.println("requestInfo==="+requestInfo);
        String uuid = requestInfo.getUserInfo().getUuid();
        AuditDetails auditDetails = commUtils.getAuditDetails(uuid, true);
        Challan challan = challanRequest.getChallan();
        System.out.println("challan---"+challan.toString());
        challan.setAuditDetails(auditDetails);
        challan.setId(UUID.randomUUID().toString());
        challan.setApplicationStatus("ACTIVE");
        if(challan.getAddress()!=null) {
        	challan.getAddress().setId(UUID.randomUUID().toString());
        	challan.getAddress().setTenantId(challan.getTenantId());
        }
        if (requestInfo.getUserInfo().getType().equalsIgnoreCase("CITIZEN"))
        challan.setAccountId(requestInfo.getUserInfo().getUuid());
        setIdgenIds(challanRequest);
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



}
