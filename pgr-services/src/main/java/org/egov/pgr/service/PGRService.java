package org.egov.pgr.service;


import org.egov.pgr.validator.ServiceRequestValidator;
import org.egov.pgr.web.models.PGREntity;
import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;

@org.springframework.stereotype.Service
public class PGRService {



    private EnrichmentService enrichmentService;

 //   private UserService userService;

    private WorkflowService workflowService;

    private ServiceRequestValidator serviceRequestValidator;







    public PGREntity create(ServiceRequest request){



        return null;
    }






}
