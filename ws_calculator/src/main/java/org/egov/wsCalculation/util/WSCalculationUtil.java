package org.egov.wsCalculation.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
import org.egov.wsCalculation.model.GetBillCriteria;
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



	@Value("${customization.allowdepreciationonnoreceipts:false}")
	Boolean allowDepreciationsOnNoReceipts;

	/**
	 * Returns the tax head search Url with tenantId and PropertyTax service name
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
	 * Returns the tax head search Url with tenantId and PropertyTax service name
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
	 * Query to fetch latest assessment for the given criteria
	 *
	 * @param assessment
	 * @return
	 */
	public String getMaxAssessmentQuery(Assessment assessment, List<Object> preparedStmtList) {

		StringBuilder query = new StringBuilder("SELECT * FROM eg_pt_assessment a1 INNER JOIN "

				+ "(select Max(createdtime) as maxtime, propertyid, assessmentyear from eg_pt_assessment WHERE Active = TRUE group by propertyid, assessmentyear) a2 "

				+ "ON a1.createdtime=a2.maxtime and a1.propertyid=a2.propertyid where a1.tenantId=? ");

		preparedStmtList.add(assessment.getTenantId());

		if (assessment.getDemandId() != null) {
			query.append(" AND a1.demandId=?");
			preparedStmtList.add(assessment.getDemandId());
		}

		if (assessment.getConnectionId() != null) {
			query.append(" AND a1.propertyId=?");
			preparedStmtList.add(assessment.getConnectionId());
		}

		if (assessment.getAssessmentYear() != null) {
			query.append(" AND a1.assessmentyear=?");
			preparedStmtList.add(assessment.getAssessmentYear());
		}

		query.append(" AND a1.active IS TRUE");

		return query.toString();
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
					.append(getBillCriteria.getPropertyId() + WSCalculationConstant.WS_CONSUMER_CODE_SEPARATOR
							+ getBillCriteria.getAssessmentNumber());

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
	 * Check if Depreciation is allowed for this Property. In case there is no
	 * receipt the depreciation will be allowed
	 * 
	 * @param assessmentYear
	 *            The year for which existing receipts needs to be checked
	 * @param tenantId
	 *            The tenantid of the property
	 * @param propertyId
	 *            The property id
	 * @param requestInfoWrapper
	 *            The incoming requestInfo
	 */

	// public Boolean isAssessmentDepreciationAllowed(String assessmentYear,
	// String tenantId, String propertyId,
	// RequestInfoWrapper requestInfoWrapper) {
	// boolean isDepreciationAllowed = false;
	// if (allowDepreciationsOnNoReceipts) {
	// List<Receipt> receipts =
	// rcptService.getReceiptsFromPropertyAndFY(assessmentYear,tenantId,
	// propertyId,
	// requestInfoWrapper);
	//
	// if (receipts.size() == 0)
	// isDepreciationAllowed = true;
	// }
	//
	// return isDepreciationAllowed;
	// }

	/**
	 * method to create demandsearch url with demand criteria
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
	


}
