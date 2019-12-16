package org.egov.bpa.config;

import java.util.TimeZone;

import javax.annotation.PostConstruct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class BPAConfiguration {


    @Value("${app.timezone}")
    private String timeZone;

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone(timeZone));
    }

  
    // User Config
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.create.path}")
    private String userCreateEndpoint;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;

    @Value("${egov.user.update.path}")
    private String userUpdateEndpoint;

    @Value("${egov.user.username.prefix}")
    private String usernamePrefix;


    //Idgen Config
    @Value("${egov.idgen.host}")
    private String idGenHost;

    @Value("${egov.idgen.path}")
    private String idGenPath;

    @Value("${egov.idgen.bpa.applicationNum.name}")
    private String applicationNoIdgenName;

    @Value("${egov.idgen.bpa.applicationNum.format}")
    private String applicationNoIdgenFormat;




    //Persister Config
    @Value("${persister.save.buildingplan.topic}")
    private String saveTopic;

   @Value("${persister.update.buildingplan.topic}")
    private String updateTopic;

    @Value("${persister.update.buildingplan.workflow.topic}")
    private String updateWorkflowTopic;

    @Value("${persister.update.buildingplan.adhoc.topic}")
    private String updateAdhocTopic;


    //Location Config
    @Value("${egov.location.host}")
    private String locationHost;

    @Value("${egov.location.context.path}")
    private String locationContextPath;

    @Value("${egov.location.endpoint}")
    private String locationEndpoint;

    @Value("${egov.location.hierarchyTypeCode}")
    private String hierarchyTypeCode;

    @Value("${egov.bpa.default.limit}")
    private Integer defaultLimit;

    @Value("${egov.bpa.default.offset}")
    private Integer defaultOffset;

    @Value("${egov.bpa.max.limit}")
    private Integer maxSearchLimit;



    // EDCR Service
    @Value("${egov.edcr.host}")
    private String edcrHost;

    @Value("${egov.edcr.authtoken.endpoint}")
    private String edcrAuthEndPoint;

    @Value("${egov.edcr.getPlan.endpoint}")
    private String getPlanEndPoint;

    //tradelicense Calculator
    @Value("${egov.tl.calculator.host}")
    private String calculatorHost;

    @Value("${egov.tl.calculator.calculate.endpoint}")
    private String calculateEndpoint;

    @Value("${egov.tl.calculator.getBill.endpoint}")
    private String getBillEndpoint;

    //Institutional key word
    @Value("${egov.ownershipcategory.institutional}")
    private String institutional;


    @Value("${egov.receipt.businessservice}")
    private String businessService;


    //Property Service
    @Value("${egov.property.service.host}")
    private String propertyHost;

    @Value("${egov.property.service.context.path}")
    private String propertyContextPath;

    @Value("${egov.property.endpoint}")
    private String propertySearchEndpoint;


    //SMS
    @Value("${kafka.topics.notification.sms}")
    private String smsNotifTopic;

    @Value("${notification.sms.enabled}")
    private Boolean isSMSEnabled;



    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.localization.statelevel}")
    private Boolean isLocalizationStateLevel;



    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsEndPoint;


    //Allowed Search Parameters
    @Value("${citizen.allowed.search.params}")
    private String allowedCitizenSearchParameters;

    @Value("${employee.allowed.search.params}")
    private String allowedEmployeeSearchParameters;



    @Value("${egov.tl.previous.allowed}")
    private Boolean isPreviousTLAllowed;

    @Value("${egov.tl.min.period}")
    private Long minPeriod;


    // Workflow
    @Value("${create.bpa.workflow.name}")
    private String businessServiceValue;

    @Value("${workflow.context.path}")
    private String wfHost;

    @Value("${workflow.transition.path}")
    private String wfTransitionPath;

    @Value("${workflow.businessservice.search.path}")
    private String wfBusinessServiceSearchPath;


    @Value("${is.external.workflow.enabled}")
    private Boolean isExternalWorkFlowEnabled;

    //USER EVENTS
	@Value("${egov.ui.app.host}")
	private String uiAppHost;
    
	@Value("${egov.usr.events.create.topic}")
	private String saveUserEventsTopic;
		
	@Value("${egov.usr.events.pay.link}")
	private String payLink;
	
	@Value("${egov.usr.events.pay.code}")
	private String payCode;
	
	@Value("${egov.user.event.notification.enabled}")
	private Boolean isUserEventsNotificationEnabled;

	@Value("${egov.usr.events.pay.triggers}")
	private String payTriggers;


}
