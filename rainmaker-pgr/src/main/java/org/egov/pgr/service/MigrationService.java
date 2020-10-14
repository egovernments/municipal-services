package org.egov.pgr.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.pgr.contract.ServiceReqSearchCriteria;
import org.egov.pgr.contract.ServiceResponse;
import org.egov.pgr.producer.PGRProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class MigrationService {



    @Value("${migration.batch.size}")
    private Long batchSize;

    @Value("${kafka.migration.topic}")
    private String migrationTopic;


    private PGRProducer producer;

    private GrievanceService grievanceService;

    @Autowired
    public MigrationService(PGRProducer producer, GrievanceService grievanceService) {
        this.producer = producer;
        this.grievanceService = grievanceService;
    }

    public void migrateData(RequestInfo requestInfo){

        List<String> tenantIds = getListOfTenantId();

        for(String tenantId : tenantIds){

            Long offset = 0l;

            while (true){

                ServiceReqSearchCriteria criteria = ServiceReqSearchCriteria.builder().offset(offset).noOfRecords(batchSize).tenantId(tenantId).build();
                ServiceResponse serviceReqResponse = (ServiceResponse) grievanceService.getServiceRequestDetailsForPlainSearch(requestInfo,criteria);

                if(CollectionUtils.isEmpty(serviceReqResponse.getServices())){
                    log.info("Records pushed for tenantId: "+tenantId+" Current offset for the tenant: "+offset);
                    break;
                }

                offset = offset + batchSize;

                producer.push(migrationTopic,serviceReqResponse);

            }



        }

    }





    private List<String> getListOfTenantId(){
        List<String> tenantIds = new LinkedList<>();

        // TO DO

        return tenantIds;


    }


}
