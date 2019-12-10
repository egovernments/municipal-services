package org.egov.swCalculation.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.BillingSlab;
import org.egov.swCalculation.model.Calculation;
import org.egov.swCalculation.model.CalculationCriteria;
import org.egov.swCalculation.model.CalculationReq;
import org.egov.swCalculation.model.Property;
import org.egov.swCalculation.model.RequestInfoWrapper;
import org.egov.swCalculation.model.SewerageConnection;
import org.egov.swCalculation.model.Slab;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.egov.swCalculation.util.CalculatorUtils;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;

@Service
@Slf4j
public class EstimationService {
	
	@Autowired
	MasterDataService mDataService;
	
	@Autowired
	CalculatorUtils calculatorUtil;
	
	@Autowired
	PayService payService;
	
	@Autowired
	SWCalculationService swCalculationService;

	
	/**
	 * Generates a List of Tax head estimates with tax head code, tax head
	 * category and the amount to be collected for the key.
	 *
	 * @param criteria
	 *            criteria based on which calculation will be done.
	 * @param requestInfo
	 *            request info from incoming request.
	 * @return Map<String, Double>
	 */
	public Map<String, List> getEstimationMap(CalculationCriteria criteria, RequestInfo requestInfo) {
		SewerageConnection sewerageConnection = null;
		String assessmentYear = getAssessmentYear();
		String tenantId = requestInfo.getUserInfo().getTenantId();
		if (criteria.getSewerageConnection() == null && !criteria.getConnectionNo().isEmpty()) {
			sewerageConnection = calculatorUtil.getSewerageConnection(requestInfo, criteria.getConnectionNo(),
					tenantId);
			criteria.setSewerageConnection(sewerageConnection);
		}
		if (criteria.getSewerageConnection() == null) {
			throw new CustomException("Sewerage Connection not found for given criteria ",
					"Sewerage Connection are not present for " + criteria.getConnectionNo() + " connection no");
		}
		Map<String, JSONArray> billingSlabMaster = new HashMap<>();
		Map<String, JSONArray> timeBasedExemptionMasterMap = new HashMap<>();
		ArrayList<String> billingSlabIds = new ArrayList<>();
		mDataService.setSewerageConnectionMasterValues(requestInfo, tenantId, billingSlabMaster,
				timeBasedExemptionMasterMap);
		BigDecimal sewarageCharge = getSewerageEstimationCharge(sewerageConnection, criteria, billingSlabMaster,
				billingSlabIds, requestInfo);
		List<TaxHeadEstimate> taxHeadEstimates = getEstimatesForTax(assessmentYear, sewarageCharge,
				criteria.getSewerageConnection(), timeBasedExemptionMasterMap,
				RequestInfoWrapper.builder().requestInfo(requestInfo).build());

		Map<String, List> estimatesAndBillingSlabs = new HashMap<>();
		estimatesAndBillingSlabs.put("estimates", taxHeadEstimates);
		estimatesAndBillingSlabs.put("billingSlabIds", billingSlabIds);
		return estimatesAndBillingSlabs;
	}
	
	/**
	 * method to do a first level filtering on the slabs based on the values
	 * present in the Sewerage Details
	 */

	public BigDecimal getSewerageEstimationCharge(SewerageConnection sewerageConnection, CalculationCriteria criteria, 
			Map<String, JSONArray> billingSlabMaster, ArrayList<String> billingSlabIds, RequestInfo requestInfo) {
		BigDecimal sewerageCharge = BigDecimal.ZERO;
		if (billingSlabMaster.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER) == null)
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Emplty");
		ObjectMapper mapper = new ObjectMapper();
		List<BillingSlab> mappingBillingSlab;
		try {
			mappingBillingSlab = mapper.readValue(
					billingSlabMaster.get(SWCalculationConstant.SW_BILLING_SLAB_MASTER).toJSONString(),
					mapper.getTypeFactory().constructCollectionType(List.class, BillingSlab.class));
		} catch (IOException e) {
			throw new CustomException("Parsing Exception", " Billing Slab can not be parsed!");
		}

		List<BillingSlab> billingSlabs = getSlabsFiltered(sewerageConnection, mappingBillingSlab, requestInfo);
		if (billingSlabs == null || billingSlabs.isEmpty())
			throw new CustomException("No Billing Slab are found on criteria ", "Billing Slab are Empty");
		if (billingSlabs.size() > 1)
			throw new CustomException("More than one Billing Slab are found on criteria ",
					"More than one billing slab found");
		//Add Billing Slab Ids
		billingSlabIds.add(billingSlabs.get(0).id);
		
		// Sewerage Charge Calculation
		Double totalUnite = 0.0;
		totalUnite = getCalculationUnit(sewerageConnection, criteria);
		if (totalUnite == 0.0)
			return sewerageCharge;
		if (isRangeCalculation(sewerageConnection.getCalculationAttribute())) {
			for (BillingSlab billingSlab : billingSlabs) {
				for (Slab slab : billingSlab.slabs) {
					if (totalUnite >= slab.from && totalUnite < slab.to) {
						sewerageCharge = BigDecimal.valueOf((totalUnite * slab.charge));
						if (slab.minimumCharge > sewerageCharge.doubleValue()) {
							sewerageCharge = BigDecimal.valueOf(slab.minimumCharge);
						}
						break;
					}
				}
			}
		} else {
			for (BillingSlab billingSlab : billingSlabs) {
				sewerageCharge = BigDecimal.valueOf(billingSlab.slabs.get(0).charge);
				break;
			}
		}
		return sewerageCharge;
	}
	
	
	public String getAssessmentYear() {
		return Integer.toString(YearMonth.now().getYear()) + "-"
				+ (Integer.toString(YearMonth.now().getYear() + 1).substring(0, 2));
	}
	
	
	/**
	 * 
	 * @param assessmentYear
	 * @param sewarageCharge
	 * @param sewerageConnection
	 * @param timeBasedExemeptionMasterMap
	 * @param requestInfoWrapper
	 * @return
	 */
	private List<TaxHeadEstimate> getEstimatesForTax(String assessmentYear, BigDecimal sewarageCharge,
			SewerageConnection sewerageConnection, Map<String, JSONArray> timeBasedExemeptionMasterMap,
			RequestInfoWrapper requestInfoWrapper) {
		List<TaxHeadEstimate> estimates = new ArrayList<>();
		// sewerage_charge
		estimates.add(TaxHeadEstimate.builder().taxHeadCode(SWCalculationConstant.SW_CHARGE)
				.estimateAmount(sewarageCharge.setScale(2, 2)).build());
		return estimates;
	}
		/**
		 * 
		 * @param waterConnection
		 * @param billingSlabs
		 * @param requestInfo
		 * @return
		 */
		private List<BillingSlab> getSlabsFiltered(SewerageConnection sewerageConnection, List<BillingSlab> billingSlabs, RequestInfo requestInfo) {
			Property property = sewerageConnection.getProperty();
			// get billing Slab
			log.debug(" the slabs count : " + billingSlabs.size());
			final String buildingType = property.getPropertyType();
			final String connectionType = sewerageConnection.getConnectionType();
			final String calculationAttribute = sewerageConnection.getCalculationAttribute();
			final String unitOfMeasurement = sewerageConnection.getUom();

			return billingSlabs.stream().filter(slab -> {
				boolean isBuildingTypeMatching = slab.BuildingType.equalsIgnoreCase(buildingType);
				boolean isConnectionTypeMatching = slab.ConnectionType.equalsIgnoreCase(connectionType);
				boolean isCalculationAttributeMatching = slab.CalculationAttribute.equalsIgnoreCase(calculationAttribute);
				boolean isUnitOfMeasurementMatcing = slab.UOM.equalsIgnoreCase(unitOfMeasurement);
				return isBuildingTypeMatching && isConnectionTypeMatching && isCalculationAttributeMatching
						&& isUnitOfMeasurementMatcing;
			}).collect(Collectors.toList());
		}
		
		private Double getCalculationUnit(SewerageConnection sewerageConnection, CalculationCriteria criteria) {
			Double totalUnite = 0.0;
			if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.meteredConnectionType)) {
				return totalUnite;
			} else if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.nonMeterdConnection)
					&& sewerageConnection.getCalculationAttribute().equalsIgnoreCase(SWCalculationConstant.noOfToilets)) {
				return totalUnite = new Double(sewerageConnection.getNoOfToilets());
			} else if (sewerageConnection.getConnectionType().equals(SWCalculationConstant.nonMeterdConnection)
					&& sewerageConnection.getCalculationAttribute().equalsIgnoreCase(SWCalculationConstant.noOfWaterClosets)) {
				return totalUnite = new Double(sewerageConnection.getNoOfWaterClosets());
			}
			return totalUnite;
		}
		/**
		 * 
		 * @param type will be calculation Attribute
		 * @return true if calculation Attribute is not Flat else false
		 */
		private boolean isRangeCalculation(String type) {
			if (type.equalsIgnoreCase(SWCalculationConstant.flatRateCalculationAttribute))
				return false;
			return true;
		}
}
