package org.egov.bpa.calculator.config;

import java.math.BigDecimal;

import lombok.Data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BPACalculatorConfig {


    @Value("${egov.billingservice.host}")
    private String billingHost;

    @Value("${egov.taxhead.search.endpoint}")
    private String taxHeadSearchEndpoint;

    @Value("${egov.taxperiod.search.endpoint}")
    private String taxPeriodSearchEndpoint;

    @Value("${egov.demand.create.endpoint}")
    private String demandCreateEndpoint;

    @Value("${egov.demand.update.endpoint}")
    private String demandUpdateEndpoint;

    @Value("${egov.demand.search.endpoint}")
    private String demandSearchEndpoint;

    @Value("${egov.bill.gen.endpoint}")
    private String billGenerateEndpoint;

    @Value("${egov.demand.minimum.payable.amount}")
    private BigDecimal minimumPayableAmount;

    @Value("${egov.demand.appl.businessservice}")
    private String applFeeBusinessService;

    @Value("${egov.demand.sanc.businessservice}")
    private String sanclFeeBusinessService;

    
    @Value("${egov.demand.lowriskpermit.businessservice}")
    private String lowRiskPermitFeeBusinessService;
  

    //BPA Registry
    @Value("${egov.bpa.host}")
    private String bpaHost;

    @Value("${egov.bpa.context.path}")
    private String bpaContextPath;
    
    @Value("${egov.edcr.host}")
    private String edcrHost;
    
    @Value("${egov.edcr.getPlan.endpoint}")
    private String planEndPoint;

    @Value("${egov.bpa.create.endpoint}")
    private String bpaCreateEndpoint;

    @Value("${egov.bpa.update.endpoint}")
    private String bpaUpdateEndpoint;

    @Value("${egov.bpa.search.endpoint}")
    private String bpaSearchEndpoint;


    //TaxHeads
    @Value("${egov.appl.fee}")
    private String baseApplFeeHead;

    @Value("${egov.sanc.fee}")
    private String baseSancFeeHead;
    
    @Value("${egov.low.appl.fee}")
    private String baseLowApplFeeHead;

    @Value("${egov.low.sanc.fee}")
    private String baseLowSancFeeHead;
    
    @Value("${egov.bpa.development.charge.head}")
    private String developmentChargeHead; 
    
    @Value("${egov.bpa.shelter.fund.head}")
    private String shelterFundHead; 
    
    @Value("${egov.bpa.scrutiny.fee.head}")
    private String scrutinyFeeHead; 
    
    @Value("${egov.bpa.labourcess.tax.head}")
    private String labourCessHead; 
    
    @Value("${egov.bpa.low.development.charge.head}")
    private String lowDevelopmentChargeHead; 
    
    @Value("${egov.bpa.low.shelter.fund.head}")
    private String lowShelterFundHead; 
    
    @Value("${egov.bpa.low.scrutiny.fee.head}")
    private String lowScrutinyFeeHead; 
    
    @Value("${egov.bpa.low.labourcess.tax.head}")
    private String lowLabourCessHead; 
    
    @Value("${egov.appl.fee.defaultAmount}")
    private String applFeeDefaultAmount;
    
    @Value("${egov.sanc.fee.defaultAmount}")
    private String sancFeeDefaultAmount;
    
    @Value("${egov.taxhead.adhoc.penalty}")
    private String adhocPenaltyTaxHead;

    @Value("${egov.taxhead.adhoc.exemption}")
    private String adhocExemptionTaxHead;


    //MDMS
    @Value("${egov.mdms.host}")
    private String mdmsHost;

    @Value("${egov.mdms.search.endpoint}")
    private String mdmsSearchEndpoint;


//    Kafka Topics
    @Value("${persister.save.bpa.calculation.topic}")
    private String saveTopic;



	@Value("${egov.bpa.ubl.grade.grampanchayat}")
	private String gramPanchayatULBGrade;
	
	@Value("${egov.bpa.ubl.grade.nagarpanchayat}")
	private String nagarPanchayatULBGrade;
	
	@Value("${egov.bpa.ubl.grade.municipalcouncil}")
	private String municipalCouncilULBGrade;
	
	@Value("${egov.bpa.ubl.grade.municipalcorporationUpto10}")
	private String municipalCorporationPopulationUpTo10LULBGrade;
	
	@Value("${egov.bpa.ubl.grade.municipalcorporationabove10}")
	private String municipalCorporationPopulationabove10LULBGrade;


}
