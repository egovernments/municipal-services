package org.egov.pgr.service;

import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.repository.IdGenRepository;
import org.egov.pgr.util.PGRUtils;
import org.egov.pgr.web.models.AuditDetails;
import org.egov.pgr.web.models.Idgen.IdResponse;
import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;
import org.egov.pgr.web.models.Workflow;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class EnrichmentService {

    private PGRUtils utils;

    private IdGenRepository idGenRepository;

    private PGRConfiguration config;

    @Autowired
    public EnrichmentService(PGRUtils utils, IdGenRepository idGenRepository, PGRConfiguration config) {
        this.utils = utils;
        this.idGenRepository = idGenRepository;
        this.config = config;
    }





    public void enrichCreateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        Workflow workflow = serviceRequest.getPgrEntity().getWorkflow();
        String tenantId = service.getTenantId();

        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(),true);

        service.setAuditDetails(auditDetails);
        service.setId(UUID.randomUUID().toString());
        service.getAddress().setId(UUID.randomUUID().toString());
        service.getAddress().setTenantId(tenantId);

        if(workflow.getVerificationDocuments()!=null){
            workflow.getVerificationDocuments().forEach(document -> {
                document.setId(UUID.randomUUID().toString());
            });
        }

        if(StringUtils.isEmpty(service.getAccountId()))
            service.setAccountId(service.getCitizen().getUuid());

        List<String> customIds = getIdList(requestInfo,tenantId,config.getServiceRequestIdGenName(),config.getServiceRequestIdGenFormat(),1);

        service.setServiceRequestId(customIds.get(0));


    }



    public void enrichUpdateRequest(ServiceRequest serviceRequest){

        RequestInfo requestInfo = serviceRequest.getRequestInfo();
        Service service = serviceRequest.getPgrEntity().getService();
        AuditDetails auditDetails = utils.getAuditDetails(requestInfo.getUserInfo().getUuid(),false);

        service.setAuditDetails(auditDetails);

    }


    /**
     * Returns a list of numbers generated from idgen
     *
     * @param requestInfo RequestInfo from the request
     * @param tenantId    tenantId of the city
     * @param idKey       code of the field defined in application properties for which ids are generated for
     * @param idformat    format in which ids are to be generated
     * @param count       Number of ids to be generated
     * @return List of ids generated using idGen service
     */
    private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey,
                                   String idformat, int count) {
        List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count).getIdResponses();

        if (CollectionUtils.isEmpty(idResponses))
            throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

        return idResponses.stream()
                .map(IdResponse::getId).collect(Collectors.toList());
    }


}
