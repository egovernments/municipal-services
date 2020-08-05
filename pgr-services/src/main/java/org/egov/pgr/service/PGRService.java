package org.egov.pgr.service;


import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.producer.Producer;
import org.egov.pgr.repository.PGRRepository;
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


    @Autowired
    public PGRService(EnrichmentService enrichmentService, UserService userService, WorkflowService workflowService,
                      ServiceRequestValidator serviceRequestValidator, ServiceRequestValidator validator, Producer producer,
                      PGRConfiguration config, PGRRepository repository) {
        this.enrichmentService = enrichmentService;
        this.userService = userService;
        this.workflowService = workflowService;
        this.serviceRequestValidator = serviceRequestValidator;
        this.validator = validator;
        this.producer = producer;
        this.config = config;
        this.repository = repository;
    }





    public PGREntity create(ServiceRequest request){
        validator.validateCreate(request);
        userService.callUserService(request);
        enrichmentService.enrichCreateRequest(request);
        producer.push(config.getCreateTopic(),request);
        return request.getPgrEntity();
    }


    public List<PGREntity> search(RequestSearchCriteria criteria){
        validator.validateSearch(criteria);

        if(criteria.getMobileNumber()!=null){
            userService.enrichUserIds(criteria);
            if(CollectionUtils.isEmpty(criteria.getUserIds()))
                return new ArrayList<>();
        }
        List<PGREntity> pgrEntities = repository.getPGREntities(criteria);
        userService.enrichUsers(pgrEntities);
        return pgrEntities;
    }


    public PGREntity update(ServiceRequest request){
        validator.validateUpdate(request);
        userService.callUserService(request);
        enrichmentService.enrichUpdateRequest(request);
        producer.push(config.getUpdateTopic(),request);
        return request.getPgrEntity();
    }





}
