package org.egov.wscalculation.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class WSCalculationConfiguration {

	@Value("${egov.ws_calculation.meterReading.default.limit}")
	private Integer meterReadingDefaultLimit;

	@Value("${egov.ws_calculation.meterReading.default.offset}")
	private Integer meterReadingDefaultOffset;

	// Idgen Config
	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;

	@Value("${egov.idgen.mr.applicationNum.name}")
	private String applicationNumberIdgenName;

	@Value("${egov.idgen.mr.applicationNum.format}")
	private String applicationNumberIdgenFormat;

	/*
	 * Calculator Configs
	 */

	// billing service
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;

	@Value("${egov.taxhead.search.endpoint}")
	private String taxheadsSearchEndpoint;

	@Value("${egov.taxperiod.search.endpoint}")
	private String taxPeriodSearchEndpoint;

	@Value("${egov.demand.create.endpoint}")
	private String demandCreateEndPoint;

	@Value("${egov.demand.update.endpoint}")
	private String demandUpdateEndPoint;

	@Value("${egov.demand.search.endpoint}")
	private String demandSearchEndPoint;

	@Value("${egov.bill.gen.endpoint}")
	private String billGenEndPoint;

	// MDMS
	@Value("${egov.mdms.host}")
	private String mdmsHost;

	@Value("${egov.mdms.search.endpoint}")
	private String mdmsEndPoint;

	public static final String Water_Time_Rebate = "Water_Time_Rebate";

	public static final String Water_Time_PENALTY = "Water_Time_PENALTY";

	public static final String Water_Time_INTEREST = "Water_Time_INTEREST";

	public static final String Water_Charge = "Water_Charge";

	public static final String Assesment_Year = "assessmentYear";

	public static final String Water_Cess = "Water_Cess";

	public static final String FINANCIALYEAR_MASTER_KEY = "2019-20";

	public static final String FINANCIAL_YEAR_STARTING_DATE = "01-Apr-2019";

	public static final String FINANCIAL_YEAR_ENDING_DATE = "01-Apr-2020";

	public static final String TAXHEADMASTER_MASTER_KEY = "WS_TAX";

	public static final String WS_Round_Off = "WS_Round_Off";

	public static final String WS_TAX_MODULE = "ws-services-calcution";

	public static final String WC_REBATE_MASTER = "Rebate";

	public static final String WC_PENANLTY_MASTER = "Penalty";

	public static final String WC_WATER_CESS_MASTER = "WaterCess";

	public static final String WC_INTEREST_MASTER = "Interest";

	public static final String CODE_FIELD_NAME = "code";

	public static final String USAGE_MAJOR_MASTER = "UsageCategoryMajor";

	public static final List<String> WS_BASED_EXEMPTION_MASTERS = Collections
			.unmodifiableList(Arrays.asList(USAGE_MAJOR_MASTER));

	// property demand configs

	@Value("${ws.module.code}")
	private String wsModuleCode;

	@Value("${pt.module.minpayable.amount}")
	private Integer ptMinAmountPayable;

	@Value("${pt.financialyear.start.month}")
	private String financialYearStartMonth;

}
