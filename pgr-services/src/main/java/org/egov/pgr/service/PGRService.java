package org.egov.pgr.service;


import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.producer.Producer;
import org.egov.pgr.repository.PGRRepository;
import org.egov.pgr.util.MDMSUtils;
import org.egov.pgr.validator.ServiceRequestValidator;
import org.egov.pgr.web.models.ServiceWrapper;
import org.egov.pgr.web.models.RequestSearchCriteria;
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


    /**
     * Creates a complaint in the system
     * @param request The service request containg the complaint information
     * @return
     */
    public ServiceRequest create(ServiceRequest request){
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateCreate(request, mdmsData);
        enrichmentService.enrichCreateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getCreateTopic(),request);
        return request;
    }


    /**
     * Searches the complaints in the system based on the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params on which to search
     * @return
     */
    public List<ServiceWrapper> search(RequestInfo requestInfo, RequestSearchCriteria criteria){
        validator.validateSearch(requestInfo, criteria);

        enrichmentService.enrichSearchRequest(requestInfo, criteria);

        if(criteria.isEmpty())
            return new ArrayList<>();

        List<ServiceWrapper> serviceWrappers = repository.getServiceWrappers(criteria);

        if(CollectionUtils.isEmpty(serviceWrappers))
            return new ArrayList<>();;

        userService.enrichUsers(serviceWrappers);
        List<ServiceWrapper> enrichedServiceWrappers = workflowService.enrichWorkflow(requestInfo,serviceWrappers);
        return enrichedServiceWrappers;
    }


    /**
     * Updates the complaint (used to forward the complaint from one application status to another)
     * @param request The request containing the complaint to be updated
     * @return
     */
    public ServiceRequest update(ServiceRequest request){
        Object mdmsData = mdmsUtils.mDMSCall(request);
        validator.validateUpdate(request, mdmsData);
        enrichmentService.enrichUpdateRequest(request);
        workflowService.updateWorkflowStatus(request);
        producer.push(config.getUpdateTopic(),request);
        return request;
    }

    /**
     * Returns the total number of comaplaints matching the given criteria
     * @param requestInfo The requestInfo of the search call
     * @param criteria The search criteria containg the params for which count is required
     * @return
     */
    public Integer count(RequestInfo requestInfo, RequestSearchCriteria criteria){
        Integer count = repository.getCount(criteria);
        return count;
    }




}
