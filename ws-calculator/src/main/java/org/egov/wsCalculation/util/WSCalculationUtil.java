package org.egov.wsCalculation.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.Assessment;
import org.egov.wsCalculation.model.AuditDetails;
import org.egov.wsCalculation.model.Demand;
import org.egov.wsCalculation.model.DemandDetail;
import org.egov.wsCalculation.model.DemandDetailAndCollection;
import org.egov.wsCalculation.model.GetBillCriteria;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.json.JSONObject;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import lombok.Getter;

@Component
@Getter
public class WSCalculationUtil {

	@Autowired
	private WSCalculationConfiguration configurations;
	
	/**
	 * Returns the tax head search Url with tenantId and WS service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxPeriodSearchUrl(String tenantId) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxPeriodSearchEndpoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
				.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSCalculationConstant.SEPARATER).append(WSCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(WSCalculationConstant.SERVICE_FIELD_VALUE_WS);
	}

	/**
	 * Returns the tax head search Url with tenantId and WS service name
	 * parameters
	 *
	 * @param tenantId
	 * @return
	 */
	public StringBuilder getTaxHeadSearchUrl(String tenantId) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getTaxheadsSearchEndpoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
				.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSCalculationConstant.SEPARATER).append(WSCalculationConstant.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(WSCalculationConstant.SERVICE_FIELD_VALUE_WS);
	}

	/**
	 * Prepares and returns Mdms search request with financial master criteria
	 *
	 * @param requestInfo
	 * @param assesmentYears
	 * @return
	 */
	public MdmsCriteriaReq getFinancialYearRequest(RequestInfo requestInfo, Set<String> assesmentYears,
			String tenantId) {

		String assessmentYearStr = StringUtils.join(assesmentYears, ",");
		MasterDetail mstrDetail = MasterDetail.builder().name(WSCalculationConstant.FINANCIAL_YEAR_MASTER)
				.filter("[?(@." + WSCalculationConstant.FINANCIAL_YEAR_RANGE_FEILD_NAME + " IN [" + assessmentYearStr
						+ "]" + " && @.module== '" + WSCalculationConstant.SERVICE_FIELD_VALUE_WS + "')]")
				.build();
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(WSCalculationConstant.FINANCIAL_MODULE)
				.masterDetails(Arrays.asList(mstrDetail)).build();
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().moduleDetails(Arrays.asList(moduleDetail)).tenantId(tenantId)
				.build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

	/**
	 * Returns the url for mdms search endpoint
	 *
	 * @return
	 */
	public StringBuilder getMdmsSearchUrl() {
		return new StringBuilder().append(configurations.getMdmsHost()).append(configurations.getMdmsEndPoint());
	}

	
	/**
	 * Returns the insert query for assessment
	 * 
	 * @return
	 */
	public String getAssessmentInsertQuery() {
		return WSCalculationConstant.QUERY_ASSESSMENT_INSERT;
	}

	/**
	 * Adds up the collection amount from the given demand and the previous advance
	 * carry forward together as new advance carry forward
	 *
	 * @param demand
	 * @return carryForward
	 */
	public BigDecimal getTotalCollectedAmountAndPreviousCarryForward(Demand demand) {

		BigDecimal carryForward = BigDecimal.ZERO;
		for (DemandDetail detail : demand.getDemandDetails()) {

			carryForward = carryForward.add(detail.getCollectionAmount());
			if (detail.getTaxHeadMasterCode().equalsIgnoreCase(WSCalculationConstant.WS_ADVANCE_CARRYFORWARD))
				carryForward = carryForward.add(detail.getTaxAmount());
		}
		return carryForward;
	}

	/**
	 * method to create demandsearch url with demand criteria
	 *
	 * @param getBillCriteria
	 * @return
	 */
	public StringBuilder getDemandSearchUrl(GetBillCriteria getBillCriteria) {

		if (CollectionUtils.isEmpty(getBillCriteria.getConsumerCodes()))
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
					.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(WSCalculationConstant.SEPARATER)
					.append(WSCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(getBillCriteria.getConnectionId() + WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR
							+ getBillCriteria.getConnectionNumber());

		else
			return new StringBuilder().append(configurations.getBillingServiceHost())
					.append(configurations.getDemandSearchEndPoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
					.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(getBillCriteria.getTenantId())
					.append(WSCalculationConstant.SEPARATER)
					.append(WSCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
					.append(StringUtils.join(getBillCriteria.getConsumerCodes(), ","));

	}

	/**
	 * Returns url for demand update Api
	 *
	 * @return
	 */
	public StringBuilder getUpdateDemandUrl() {
		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getDemandUpdateEndPoint());
	}

	/**
	 * method to create demand search url with demand criteria
	 *
	 * @param assessment
	 * @return
	 */
	public StringBuilder getDemandSearchUrl(Assessment assessment) {

		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getDemandSearchEndPoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
				.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(assessment.getTenantId())
				.append(WSCalculationConstant.SEPARATER).append(WSCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(assessment.getConnectionId() + WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR
						+ assessment.getAssessmentNumber());
	}

	public AuditDetails getAuditDetails(String by, boolean isCreate) {
		Long time = new Date().getTime();

		if (isCreate)
			return AuditDetails.builder().createdBy(by).createdTime(time).lastModifiedBy(by).lastModifiedTime(time)
					.build();
		else
			return AuditDetails.builder().lastModifiedBy(by).lastModifiedTime(time).build();
	}
	
	/**
	 * Returns url for Bill Gen Api
	 *
	 * @param tenantId
	 * @param demandId
	 * @return
	 */
	public StringBuilder getBillGenUrl(String tenantId, String demandId, String consumerCode) {
		return new StringBuilder().append(configurations.getBillingServiceHost())
				.append(configurations.getBillGenEndPoint()).append(WSCalculationConstant.URL_PARAMS_SEPARATER)
				.append(WSCalculationConstant.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSCalculationConstant.SEPARATER).append(WSCalculationConstant.DEMAND_ID_SEARCH_FIELD_NAME)
				.append(demandId).append(WSCalculationConstant.SEPARATER)
				.append(WSCalculationConstant.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(WSCalculationConstant.WATER_TAX_SERVICE_CODE).append(WSCalculationConstant.SEPARATER)
				.append(WSCalculationConstant.CONSUMER_CODE_SEARCH_FIELD_NAME).append(consumerCode);
	}
	
	/**
	 * Creates generate bill url using tenantId,consumerCode and businessService
	 * 
	 * @return Bill Generate url
	 */
	public String getBillGenerateURI() {
		StringBuilder url = new StringBuilder(configurations.getBillingServiceHost());
		url.append(configurations.getBillGenerateEndpoint());
		url.append("?");
		url.append("tenantId=");
		url.append("{1}");
		url.append("&");
		url.append("consumerCode=");
		url.append("{2}");
		url.append("&");
		url.append("businessService=");
		url.append("{3}");

		return url.toString();
	}
	
	/**
	 * 
	 * @param demand
	 * @return The Applicable tax amount for demand
	 */
	public BigDecimal getTaxAmtFromDemandForApplicablesGeneration(Demand demand) {
		BigDecimal taxAmount = BigDecimal.ZERO;
		for(DemandDetail detail : demand.getDemandDetails()) {
			if (WSCalculationConstant.TAX_APPLICABLE.contains(detail.getTaxHeadMasterCode())) {
				taxAmount = taxAmount.add(detail.getTaxAmount());
			}
		}
		return taxAmount;
	}

	
	public DemandDetailAndCollection getLatestDemandDetailByTaxHead(String taxHeadCode,
			List<DemandDetail> demandDetails) {
		List<DemandDetail> details = demandDetails.stream()
				.filter(demandDetail -> demandDetail.getTaxHeadMasterCode().equalsIgnoreCase(taxHeadCode))
				.collect(Collectors.toList());
		if (CollectionUtils.isEmpty(details))
			return null;

		BigDecimal taxAmountForTaxHead = BigDecimal.ZERO;
		BigDecimal collectionAmountForTaxHead = BigDecimal.ZERO;
		DemandDetail latestDemandDetail = null;
		long maxCreatedTime = 0l;

		for (DemandDetail detail : details) {
			taxAmountForTaxHead = taxAmountForTaxHead.add(detail.getTaxAmount());
			collectionAmountForTaxHead = collectionAmountForTaxHead.add(detail.getCollectionAmount());
			if (detail.getAuditDetails().getCreatedTime() > maxCreatedTime) {
				maxCreatedTime = detail.getAuditDetails().getCreatedTime();
				latestDemandDetail = detail;
			}
		}

		return DemandDetailAndCollection.builder().taxHeadCode(taxHeadCode).latestDemandDetail(latestDemandDetail)
				.taxAmountForTaxHead(taxAmountForTaxHead).collectionAmountForTaxHead(collectionAmountForTaxHead)
				.build();

	}
	

	
}
