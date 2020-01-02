package org.egov.swCalculation.config;

import java.math.BigDecimal;

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
public class SWCalculationConfiguration {

	// billing service
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;

	@Value("${egov.taxhead.search.endpoint}")
	private String taxheadsSearchEndpoint;

	@Value("${egov.taxperiod.search.endpoint}")
	private String taxPeriodSearchEndpoint;

	// MDMS
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndPoint;

	// sewerage Registry
	@Value("${egov.sw.host}")
	private String sewerageConnectionHost;

	@Value("${egov.sc.search.endpoint}")
	private String sewerageConnectionSearchEndPoint;

	@Value("${sw.module.minpayable.amount}")
	private BigDecimal swMinAmountPayable;

	@Value("${egov.demand.businessservice}")
	private String businessService;
	
	@Value("${egov.demand.create.endpoint}")
	private String demandCreateEndPoint;
	
	@Value("${egov.demand.update.endpoint}")
	private String demandUpdateEndPoint;
	
	@Value("${egov.demand.search.endpoint}")
	private String demandSearchEndPoint;

	@Value("${egov.sewerageservice.pagination.default.limit}")
	private String limit;
	
	@Value("${egov.sewerageservice.pagination.default.offset}")
	private String offset;
	
	@Value("${egov.bill.gen.endpoint}")
	private String billGenEndPoint;
	
	@Value("${egov.demand.billexpirytime}")
	private Long demandBillExpiry;
	
	
    //SMS
    @Value("${kafka.topics.notification.sms}")
    private String smsNotifTopic;

    @Value("${notification.sms.enabled}")
    private Boolean isSMSEnabled;
    
    
    //Email
    @Value("${notification.mail.enabled}")
    private Boolean isMailEnabled;
    
    
    @Value("${kafka.topics.notification.mail.name}")
    private String emailNotifyTopic;
   


    //Localization
    @Value("${egov.localization.host}")
    private String localizationHost;

    @Value("${egov.localization.context.path}")
    private String localizationContextPath;

    @Value("${egov.localization.search.endpoint}")
    private String localizationSearchEndpoint;

    @Value("${egov.localization.statelevel}")
    private Boolean isLocalizationStateLevel;
    
    @Value("${sw.calculator.demand.successful}")
    private String onDemandSuccess;
    
    @Value("${sw.calculator.demand.failed}")
    private String onDemandFailed;
    
  
	
}
