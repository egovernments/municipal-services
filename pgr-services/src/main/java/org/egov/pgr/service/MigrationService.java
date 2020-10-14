package org.egov.pgr.service;


import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.User;
import org.egov.pgr.config.PGRConfiguration;
import org.egov.pgr.producer.Producer;
import org.egov.pgr.web.models.*;
import org.egov.pgr.web.models.pgrV1.*;
import org.egov.pgr.web.models.pgrV1.Address;
import org.egov.pgr.web.models.pgrV1.Service;
import org.egov.pgr.web.models.pgrV1.ServiceResponse;
import org.egov.pgr.web.models.workflow.ProcessInstance;
import org.egov.pgr.web.models.workflow.ProcessInstanceRequest;
import org.egov.pgr.web.models.workflow.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;

import static org.egov.pgr.util.PGRConstants.IMAGE_DOCUMENT_TYPE;

@Component
@Slf4j
public class MigrationService {



    @Autowired
    private Producer producer;

    @Autowired
    private PGRConfiguration config;

    private final Map<String,String>  oldToNewStatus = new HashMap<String,String>(){{

            put("open", "OPEN");
            put("assigned", "ASSIGNED");
            put("closed", "CLOSED");
            put("rejected", "REJECTED");
            put("resolved", "RESOLVED");
            put("reassignrequested", "PENDINGFORREASSIGNMENT");

        }
    };



    public void migrate(ServiceResponse serviceResponse){


        List<Service> servicesV1 = serviceResponse.getServices();
        List<ActionHistory> actions = serviceResponse.getActionHistory();

    }

    /**
     * Data Assumptions:
     * All records have actionHistory
     * Is AuditDetails of old address different from service auditDetails
     */

    /*
    *
    * Skipping records with empty actionHistory as no linking with service is possible in that case
    * Images are added in workflow doument with documentType as PHOTO which is defined in constants file
    * Citizen object is not migrated as it is stored in user service only it's reference i.e accountId is migrated
    *
    *
    * */

    /**
     *
     * @param servicesV1
     * @param actionHistories
     * @return
     */
    private void transform(List<Service> servicesV1, List<ActionHistory> actionHistories){


        Map<String,List<ActionInfo>> idToActionMap = new HashMap<>();

        for(ActionHistory actionHistory : actionHistories) {
            List<ActionInfo> actions = actionHistory.getActions();

            if(CollectionUtils.isEmpty(actions))
                log.error("Skiping record with empty actionHistory");

            String id = actions.get(0).getBusinessKey();
            idToActionMap.put(id,actions);
        }

        for(Service serviceV1 : servicesV1){

            List<ActionInfo> actionInfos = idToActionMap.get(serviceV1.getServiceRequestId());
            List<ProcessInstance> workflows = new LinkedList<>();

            org.egov.pgr.web.models.Service service = transformService(serviceV1);

            actionInfos.forEach(actionInfo -> {
                ProcessInstance workflow = transformAction(actionInfo);
                workflows.add(workflow);
            });

            ProcessInstanceRequest processInstanceRequest = ProcessInstanceRequest.builder().processInstances(workflows).build();
            ServiceRequest serviceRequest = ServiceRequest.builder().service(service).build();

            producer.push(config.getCreateTopic(),serviceRequest);
            producer.push(config.getWorkflowSaveTopic(),processInstanceRequest);
        }


    }


    private org.egov.pgr.web.models.Service transformService(Service serviceV1){

        String tenantId = serviceV1.getTenantId();
        String serviceCode = serviceV1.getServiceCode();
        String serviceRequestId = serviceV1.getServiceRequestId();
        String description = serviceV1.getDescription();
        String source = serviceV1.getSource().toString();
        String rating = serviceV1.getRating();


        /**
         * AccountId is id not uuid in old pgr mapping has to fetched
         * of id to uuid
         */
        String accountId = serviceV1.getAccountId();


        AuditDetails auditDetails = serviceV1.getAuditDetails();

        Object attributes = serviceV1.getAttributes();


        Double latitude = serviceV1.getLat();
        Double longitutude = serviceV1.getLongitutde();

        /**
         * Transform address and set geo location
         */
        GeoLocation geoLocation = GeoLocation.builder().longitude(longitutude).latitude(latitude).build();
        org.egov.pgr.web.models.Address address = transformAddress(serviceV1.getAddressDetail());
        address.setGeoLocation(geoLocation);

        /**
         * FIXME
         * Active flag has to be accommodated
         */
        Boolean active = serviceV1.getActive();

        org.egov.pgr.web.models.Service service = org.egov.pgr.web.models.Service.builder()
                .tenantId(tenantId)
                .accountId(accountId)
                .additionalDetail(attributes)
                .serviceCode(serviceCode)
                .serviceRequestId(serviceRequestId)
                .description(description)
                .source(source)
                .address(address)
                .auditDetails(auditDetails)
                .build();

        if(org.apache.commons.lang3.StringUtils.isNumeric(rating)){
                service.setRating(Integer.parseInt(rating));
        }


        return service;

    }

    /**
     *  No auditDetails in address
     *  Geolocation will be enriched in service transform as that data is available there
     * @param addressV1
     * @return
     */
    private org.egov.pgr.web.models.Address transformAddress(Address addressV1){

        String id = addressV1.getUuid();
        String locality = addressV1.getMohalla();
        String colony = addressV1.getLocality();
        String city = addressV1.getCity();
        String landmark = addressV1.getLandmark();
        String houseNoAndStreetName = addressV1.getHouseNoAndStreetName();

        /**
         * FIXME : No auditDetails in new address object
          */

        AuditDetails auditDetails = addressV1.getAuditDetails();

        /**
         * FIXME : houseNoAndStreetName and colony mapping has to be corrected
         */

        org.egov.pgr.web.models.Address address = org.egov.pgr.web.models.Address.builder()
                .id(id)
                .locality(Boundary.builder().code(locality).build())
                .city(city)
                .landmark(landmark)
                .street(houseNoAndStreetName)
                .region(colony)
                .build();

        return address;

    }





    private ProcessInstance transformAction(ActionInfo actionInfo){

        String uuid = actionInfo.getUuid();
        String createdBy = actionInfo.getBy();
        Long createdTime = actionInfo.getWhen();
        String businessId = actionInfo.getBusinessKey();
        String action = actionInfo.getAction();
        String status = actionInfo.getStatus();
        String assignee = actionInfo.getAssignee();
        String comments = actionInfo.getComment();
        List<String> fileStoreIds = actionInfo.getMedia();


        State state = State.builder().state(oldToNewStatus.get(status)).build();

        // LastmodifiedTime and by is same as that for created as every time new entry is created whenever any action is taken
        org.egov.pgr.web.models.AuditDetails auditDetails = org.egov.pgr.web.models.AuditDetails.builder().createdBy(createdBy)
                .createdTime(createdTime).lastModifiedBy(createdBy).lastModifiedTime(createdTime).build();

        ProcessInstance workflow = ProcessInstance.builder()
                                    .id(uuid)
                                    .action(action)
                                    .comment(comments)
                                    .businessId(businessId)
                                    .state(state)
                                    .auditDetails(auditDetails)
                                    .build();


        // Wrapping assignee uuid in User object to add it in workflow
        if(!StringUtils.isEmpty(assignee)){
            User user = new User();
            user.setUuid(assignee);
            workflow.setAssignes(Collections.singletonList(user));
        }

        // Setting the images uploaded in workflow document
        if(!CollectionUtils.isEmpty(fileStoreIds)){
            List<Document> documents = new LinkedList<>();
            for (String fileStoreId : fileStoreIds){
                Document document = Document.builder()
                                    .documentType(IMAGE_DOCUMENT_TYPE)
                                    .fileStore(fileStoreId)
                                    .id(UUID.randomUUID().toString())
                                    .build();
                documents.add(document);
            }
            workflow.setDocuments(documents);
        }

        return workflow;
    }



}
