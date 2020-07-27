package org.egov.pgr.service;

import org.egov.pgr.web.models.Service;
import org.egov.pgr.web.models.ServiceRequest;

@org.springframework.stereotype.Service
public class EnrichmentService {






    public void enrichCreateRequest(ServiceRequest serviceRequest){

        Service service = serviceRequest.getPgrEntity().getService();



    }



}
