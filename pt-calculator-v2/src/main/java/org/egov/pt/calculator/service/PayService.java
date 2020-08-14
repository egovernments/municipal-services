package org.egov.pt.calculator.service;

import java.math.BigDecimal;
import java.util.*;

import static org.egov.pt.calculator.util.CalculatorConstants.*;
import org.egov.pt.calculator.util.CalculatorUtils;
import org.egov.pt.calculator.web.models.TaxHeadEstimate;
import org.egov.pt.calculator.web.models.collections.Payment;
import org.egov.pt.calculator.web.models.demand.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import net.minidev.json.JSONArray;

/**
 * Deals with the functionality that are performed 
 * 
 * before the time of bill generation(before payment)  
 * 
 * or at the time of bill apportioning(after payment)
 * 
 * @author kavi elrey
 *
 */
@Service
public class PayService {

	@Autowired
	private CalculatorUtils utils;

	/**
	 * Updates the incoming demand with latest rebate, penalty and interest values if applicable
	 * If the demand details are not already present then new demand details will be added
	 * Assumption : Partial payment is not allowed
	 * 
	 * @param assessmentYear
	 * @return
	 */

	public Map<String, BigDecimal> applyPenaltyRebateAndInterest(Demand demand, List<Payment> payments,
			List<TaxPeriod> taxPeriods, Map<String, JSONArray> jsonMasterMap) {

		Map<String, BigDecimal> estimates = new HashMap<>();
		boolean demandNotCollected = true;
		BigDecimal rebate = BigDecimal.ZERO;
		BigDecimal promotionalRebate = BigDecimal.ZERO;
		BigDecimal interest = BigDecimal.ZERO;

		for (DemandDetail demandDetail : demand.getDemandDetails()) {
			if (demandDetail.getTaxHeadMasterCode().equalsIgnoreCase("PT_TAX")) {
				if (demandDetail.getCollectionAmount().compareTo(BigDecimal.ZERO) == 1) {
					demandNotCollected = false;
				}
			}
		}

		//Rebate Calculation
		if (demandNotCollected) {
			rebate = getRebate(demand, jsonMasterMap.get(REBATE_MASTER), payments);
			promotionalRebate = getRebate(demand, jsonMasterMap.get(PROMOTIONAL_REBATE_MASTER), payments);

			estimates.put(PT_TIME_REBATE, rebate.setScale(2, 2).negate());
			estimates.put(PT_PROMOTIONAL_REBATE, promotionalRebate.setScale(2, 2).negate());
		}

		//Interest Calculation
		if (demandNotCollected) {
			interest = getInterestCurrent(demand, payments, taxPeriods, jsonMasterMap.get(INTEREST_MASTER));
			estimates.put(PT_TIME_INTEREST, interest.setScale(2, 2));
		}
		
		return estimates;
	}

	/**
	 * Returns the Amount of Rebate that can be applied on the given tax amount for
	 * the given period
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @return
	 */
	public BigDecimal getRebate(Demand demand, List<Object> rebateMasterList, List<Payment> payments) {

		BigDecimal rebateAmt = BigDecimal.ZERO;
		BigDecimal taxAmt = BigDecimal.ZERO;
		//BigDecimal taxAmt = getTaxAmountToCalculateRebateOnApplicables(demand, payments);
		//Assumtion: Partial payment is not allowed 
		for (DemandDetail demandDetail : demand.getDemandDetails()) {
			if (demandDetail.getTaxHeadMasterCode().equalsIgnoreCase("PT_TAX")) {
					taxAmt = demandDetail.getTaxAmount();
			}
		}

		long currentTime = Calendar.getInstance().getTimeInMillis();
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
		String currentFinancialYear = currentMonth < 4 ? (currentYear - 1) + "-" + currentYear
				: currentYear + "-" + (currentYear + 1);

		Calendar fromCalendar = Calendar.getInstance();
		fromCalendar.setTimeInMillis(demand.getTaxPeriodFrom());
		int fromYear = fromCalendar.get(Calendar.YEAR);
		Calendar toCalendar = Calendar.getInstance();
		toCalendar.setTimeInMillis(demand.getTaxPeriodTo());
		int toYear = toCalendar.get(Calendar.YEAR);
		String demandFinancialYear = fromYear + "-" + toYear;
		
		if (currentFinancialYear.equals(demandFinancialYear)) {

			for (Object rebate : rebateMasterList) {
				Map<String, Object> rebateMap = (Map<String, Object>) rebate;
				if ( (long)rebateMap.get("startingDay") < currentTime
						&& currentTime < (long)rebateMap.get("endingDay")) {
					rebateAmt = taxAmt.multiply(BigDecimal.valueOf((double) rebateMap.get("rate"))).divide(HUNDRED);
				}
			}
		}
		return rebateAmt;
	}

	/**
	 * Returns the Amount of penalty that has to be applied on the given tax amount for the given period
	 * If paid after April 1st full year interest on the amount is caluculated
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @return
	 */
	private BigDecimal getInterestCurrent(Demand demand, List<Payment> payments, List<TaxPeriod> taxPeriods,
			JSONArray jsonArray) {

		BigDecimal interestAmt = BigDecimal.ZERO;

		for (TaxPeriod taxPeriod : taxPeriods) {
			BigDecimal interestPerTaxPeriod = BigDecimal.ZERO;
			if (taxPeriod.getFromDate() > demand.getTaxPeriodFrom()) {
				BigDecimal taxAmt = getTaxAmountToCalculateInterestOnApplicables(taxPeriod.getFromDate(), demand, payments);
				BigDecimal interestRateForTaxperiod = utils.getInterestRateForTaxperiod(taxPeriod.getFinancialYear(), jsonArray);
				interestPerTaxPeriod = taxAmt.multiply(interestRateForTaxperiod.divide(HUNDRED));
			}
			interestAmt = interestAmt.add(interestPerTaxPeriod);
		}

		return interestAmt;
	}

	/**
	 * Calculates the tax amount on which interest need to be calculated. 
	 * TODO: check below
	 */
	private BigDecimal getTaxAmountToCalculateInterestOnApplicables(Long fromDate, Demand demand,
			List<Payment> payments) {

		if (payments == null || CollectionUtils.isEmpty(payments))
			return utils.getTaxAmtFromDemandForApplicablesGeneration(demand);
		else {

			BigDecimal taxAmt = BigDecimal.ZERO;
			BigDecimal amtPaid = BigDecimal.ZERO;

			List<BillAccountDetail> billAccountDetails = new LinkedList<>();

			payments.forEach(payment -> {
				if (payment.getTransactionDate() < fromDate) {
					payment.getPaymentDetails().forEach(paymentDetail -> {
						if (paymentDetail.getBusinessService().equalsIgnoreCase(SERVICE_FIELD_VALUE_PT)) {
							paymentDetail.getBill().getBillDetails().forEach(billDetail -> {
								billAccountDetails.addAll(billDetail.getBillAccountDetails());
							});
						}
					});
				}
			});

			for (BillAccountDetail detail : billAccountDetails) {
				if (TAXES_TO_BE_CONSIDERD.contains(detail.getTaxHeadCode())) {
					taxAmt = taxAmt.add(detail.getAmount());
					amtPaid = amtPaid.add(detail.getAdjustedAmount());
				}
			}
			return taxAmt.subtract(amtPaid);
		}
	}

	/**
	 * Decimal is ceiled for all the tax heads
	 * 
	 * if the decimal is greater than 0.5 upper bound will be applied
	 * 
	 * else if decimal is lesser than 0.5 lower bound is applied
	 * 
	 */
	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount) {

		BigDecimal roundOffPos = BigDecimal.ZERO;
		BigDecimal roundOffNeg = BigDecimal.ZERO;

		BigDecimal result = creditAmount.add(debitAmount);
		BigDecimal roundOffAmount = result.setScale(2, 2);
		BigDecimal reminder = roundOffAmount.remainder(BigDecimal.ONE);

		if (reminder.doubleValue() >= 0.5)
			roundOffPos = roundOffPos.add(BigDecimal.ONE.subtract(reminder));
		else if (reminder.doubleValue() < 0.5)
			roundOffNeg = roundOffNeg.add(reminder).negate();

		if (roundOffPos.doubleValue() > 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffPos)
					.taxHeadCode(PT_ROUNDOFF).build();
		else if (roundOffNeg.doubleValue() < 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffNeg)
					.taxHeadCode(PT_ROUNDOFF).build();
		else
			return null;
	}

	public TaxHeadEstimate roundOffDecimals(BigDecimal amount,BigDecimal currentRoundOff) {

		BigDecimal roundOff = BigDecimal.ZERO;

		BigDecimal roundOffAmount = amount.setScale(2, 2);
		BigDecimal reminder = roundOffAmount.remainder(BigDecimal.ONE);

		if (reminder.doubleValue() >= 0.5)
			roundOff = roundOff.add(BigDecimal.ONE.subtract(reminder));
		else if (reminder.doubleValue() < 0.5)
			roundOff = roundOff.add(reminder).negate();
		
		if (!(currentRoundOff==null && roundOff.doubleValue() == 0))
			return TaxHeadEstimate.builder().estimateAmount(roundOff)
					.taxHeadCode(PT_ROUNDOFF).build();
		else
			return null;
	}
	
}
