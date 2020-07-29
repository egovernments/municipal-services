package org.egov.pgr.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.util.PGRUtils;
import org.egov.pgr.web.models.AuditDetails;
import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;
import org.springframework.util.StringUtils;

import java.util.UUID;

@org.springframework.stereotype.Service
public class EnrichmentService {



    private PGRUtils utils;


    public void enrichCreateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(),true);

        service.setAuditDetails(auditDetails);
        service.setId(UUID.randomUUID().toString());
        service.getAddress().setId(UUID.randomUUID().toString());

        if(StringUtils.isEmpty(service.getAccountId()))
            service.setAccountId(service.getCitizen().getUuid());
        

    }



    public void enrichUpdateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(),false);

        service.setAuditDetails(auditDetails);

    }



}
