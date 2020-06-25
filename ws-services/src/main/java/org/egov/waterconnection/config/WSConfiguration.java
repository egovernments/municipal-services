package org.egov.waterconnection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Component
public class WSConfiguration {

	@Value("${egov.waterservice.pagination.default.limit}")
	private Integer defaultLimit;

	@Value("${egov.waterservice.pagination.default.offset}")
	private Integer defaultOffset;

	@Value("${egov.waterservice.pagination.max.limit}")
	private Integer maxLimit;

	// IDGEN
	@Value("${egov.idgen.wcid.name}")
	private String waterConnectionIdGenName;

	@Value("${egov.idgen.wcid.format}")
	private String waterConnectionIdGenFormat;

	@Value("${egov.idgen.wcapid.name}")
	private String waterApplicationIdGenName;

	@Value("${egov.idgen.wcapid.format}")
	private String waterApplicationIdGenFormat;

	// Idgen Config
	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;

	// Workflow
	@Value("${create.ws.workflow.name}")
	private String businessServiceValue;

	@Value("${workflow.context.path}")
	private String wfHost;

	@Value("${workflow.transition.path}")
	private String wfTransitionPath;

	@Value("${workflow.businessservice.search.path}")
	private String wfBusinessServiceSearchPath;
	
	@Value("${workflow.process.search.path}")
	private String wfProcessSearchPath;

	@Value("${is.external.workflow.enabled}")
	private Boolean isExternalWorkFlowEnabled;

	@Value("${egov.waterservice.updatewaterconnection.workflow.topic}")
	private String workFlowUpdateTopic;

	// Localization
	@Value("${egov.localization.host}")
	private String localizationHost;

	@Value("${egov.localization.context.path}")
	private String localizationContextPath;

	@Value("${egov.localization.search.endpoint}")
	private String localizationSearchEndpoint;

	@Value("${egov.localization.statelevel}")
	private Boolean isLocalizationStateLevel;

	// SMS
	@Value("${kafka.topics.notification.sms}")
	private String smsNotifTopic;

	@Value("${notification.sms.enabled}")
	private Boolean isSMSEnabled;

	@Value("${notification.sms.link}")
	private String smsNotificationLink;

	@Value("${notification.url}")
	private String notificationUrl;

	@Value("${egov.usr.events.create.topic}")
	private String saveUserEventsTopic;

	// Water Topic
	@Value("${egov.waterservice.createwaterconnection}")
	private String onWaterSaved;

	@Value("${egov.waterservice.updatewaterconnection}")
	private String onWaterUpdated;

	@Value("${egov.user.event.notification.enabled}")
	private Boolean isUserEventsNotificationEnabled;
	
    //User Configuration
    @Value("${egov.user.host}")
    private String userHost;

    @Value("${egov.user.context.path}")
    private String userContextPath;

    @Value("${egov.user.search.path}")
    private String userSearchEndpoint;
    
    // water connection Calculator
    @Value("${egov.ws.calculation.host}")
    private String calculatorHost;

    @Value("${egov.ws.calculation.endpoint}")
    private String calculateEndpoint;
    
    @Value("${egov.receipt.businessservice}")
    private String receiptBusinessservice;
    
    @Value("${ws.meterreading.create}")
    private String createMeterReading;
    
    @Value("${ws.meterreading.create.endpoint}")
    private String createMeterReadingEndpoint;
    
    @Value("${ws.mseva.app.link}")
    private String mSevaAppLink;
    
    @Value("${ws.view.history.link}")
    private String viewHistoryLink;
    
    @Value("${ws.connectiondetails.link}")
    private String connectionDetailsLink;
    
    @Value("${ws.application.pay.link}")
    private String applicationPayLink;
    
    @Value("${egov.ws.estimate.endpoint}")
    private String estimationEndpoint;
    
    @Value("${ws.pdfservice.link}")
    private String pdfServiceLink;
    
    @Value("${ws.fileStore.link}")
    private String fileStoreLink;
    
    @Value("${ws.shortener.url}")
    private String shortenerURL;
    
    @Value("${egov.pdfservice.host}")
    private String pdfServiceHost;
    
    @Value("${egov.filestore.host}")
    private String fileStoreHost;
    
    @Value("${ws.editnotification.topic}")
    private String editNotificationTopic;
    
	@Value("${ws.consume.filestoreids}")
	private String fileStoreIdsTopic;

	@Value("${egov.waterservice.savefilestoreIds}")
	private String saveFileStoreIdsTopic;
   
}
