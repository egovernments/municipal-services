package org.egov.pgr.service;


import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.producer.Producer;
import org.egov.pgr.repository.PGRRepository;
import org.egov.pgr.util.MDMSUtils;
import org.egov.pgr.validator.ServiceRequestValidator;
import org.egov.pgr.web.models.PGREntity;
import org.egov.pgr.web.models.RequestSearchCriteria;
import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class PGRService {



    private EnrichmentService enrichmentService;

    private UserService userService;

    private WorkflowService workflowService;

    private ServiceRequestValidator serviceRequestValidator;

    private ServiceRequestValidator validator;

    private Producer producer;

    private PGRConfiguration config;

    private PGRRepository repository;

    private MDMSUtils mdmsUtils;


    @Autowired
    public PGRService(EnrichmentService enrichmentService, UserService userService, WorkflowService workflowService,
                      ServiceRequestValidator serviceRequestValidator, ServiceRequestValidator validator, Producer producer,
                      PGRConfiguration config, PGRRepository repository, MDMSUtils mdmsUtils) {
        this.enrichmentService = enrichmentService;
        this.userService = userService;
        this.workflowService = workflowService;
        this.serviceRequestValidator = serviceRequestValidator;
        this.validator = validator;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
        this.mdmsUtils = mdmsUtils;
    }





    public PGREntity create(ServiceRequest request){
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateCreate(request, mdmsData);
        userService.callUserService(request);
        enrichmentService.enrichCreateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request);
        return request.getPgrEntity();
    }


    public List<PGREntity> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearch(criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        List<PGREntity> pgrEntities = repository.getPGREntities(criteria);

        if(CollectionUtils.isEmpty(pgrEntities))
            return new ArrayList<>();;

        userService.enrichUsers(pgrEntities);
        workflowService.enrichWorkflow(requestInfo,pgrEntities);
        return pgrEntities;
    }


    public PGREntity update(ServiceRequest request){
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateUpdate(request, mdmsData);
        userService.callUserService(request);
        workflowService.updateWorkflowStatus(request);
        enrichmentService.enrichUpdateRequest(request);
        producer.push(config.getUpdateTopic(),request);
        return request.getPgrEntity();
    }

    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        Integer count = repository.getCount(criteria);
        return count;
    }




}
