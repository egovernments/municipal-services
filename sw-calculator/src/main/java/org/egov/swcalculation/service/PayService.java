package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.TaxHeadEstimate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONArray;

@Service
public class PayService {

	@Autowired
	private MasterDataService mDService;
	
	@Autowired
	private EstimationService estimationService;
		
	
	/**
	 * Returns the Amount of Rebate that can be applied on the given tax amount for
	 * the given period
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @returnroundOfDecimals
	 */
	public BigDecimal getRebate(BigDecimal taxAmt, String assessmentYear, JSONArray rebateMasterList) {

		Map<String, Object> rebate = mDService.getApplicableMaster(assessmentYear, rebateMasterList);

		if (null == rebate)
			return BigDecimal.ZERO;

		String[] time = ((String) rebate.get(SWCalculationConstant.ENDING_DATE_APPLICABLES)).split("/");
		setDateToCalendar(assessmentYear, time, Calendar.getInstance());

		if (Calendar.getInstance().getTimeInMillis() > System.currentTimeMillis()) {
			return mDService.calculateApplicables(taxAmt, rebate);
		}
		return BigDecimal.ZERO;
	}
	
	/**
	 * Sets the date in to calendar based on the month and date value present in the
	 * time array
	 * 
	 * @param assessmentYear
	 * @param time
	 * @param cal
	 */
	private void setDateToCalendar(String assessmentYear, String[] time, Calendar cal) {

		cal.clear();
		Integer day = Integer.valueOf(time[0]);
		Integer month = Integer.valueOf(time[1]) - 1;
		// One is subtracted because calender reads january as 0
		Integer year = Integer.valueOf(assessmentYear.split("-")[0]);
		if (month < 3)
			year += 1;
		cal.set(year, month, day);
	}	
	/**
	 * 
	 * @param creditAmount
	 * @param debitAmount
	 * @return TaxHead for SW round off
	 */
	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount, boolean isConnectionFee) {
		BigDecimal roundOffPos = BigDecimal.ZERO;
		BigDecimal roundOffNeg = BigDecimal.ZERO;
		String taxHead = isConnectionFee == true ? SWCalculationConstant.SW_Round_Off
				: SWCalculationConstant.SW_ONE_TIME_FEE_ROUND_OFF;
		BigDecimal result = creditAmount.add(debitAmount);
		BigDecimal roundOffAmount = result.setScale(2, 2);
		BigDecimal reminder = roundOffAmount.remainder(BigDecimal.ONE);

		if (reminder.doubleValue() >= 0.5)
			roundOffPos = roundOffPos.add(BigDecimal.ONE.subtract(reminder));
		else if (reminder.doubleValue() < 0.5)
			roundOffNeg = roundOffNeg.add(reminder).negate();

		if (roundOffPos.doubleValue() > 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffPos).taxHeadCode(taxHead).build();
		else if (roundOffNeg.doubleValue() < 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffNeg).taxHeadCode(taxHead).build();
		else
			return null;
		
	}
	
	
	/**
	 * 
	 * @param waterCharge
	 * @param assessmentYear
	 * @param timeBasedExmeptionMasterMap
	 * @param billingExpiryDate
	 * @return estimation of time based exemption
	 */
	public Map<String, BigDecimal> applyPenaltyRebateAndInterest(BigDecimal waterCharge,
			String assessmentYear, Map<String, JSONArray> timeBasedExmeptionMasterMap, Long billingExpiryDate) {

		if (BigDecimal.ZERO.compareTo(waterCharge) >= 0)
			return Collections.emptyMap();
		Map<String, BigDecimal> estimates = new HashMap<>();
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal interest = BigDecimal.ZERO;
		long currentUTC = System.currentTimeMillis();
		long numberOfDaysInMillies = billingExpiryDate - currentUTC;
		BigDecimal noOfDays = BigDecimal.valueOf((TimeUnit.MILLISECONDS.toDays(Math.abs(numberOfDaysInMillies))));
		if(BigDecimal.ONE.compareTo(noOfDays) <= 0) noOfDays = noOfDays.add(BigDecimal.ONE);
		penalty = getApplicablePenalty(waterCharge, noOfDays, timeBasedExmeptionMasterMap.get(SWCalculationConstant.SW_PENANLTY_MASTER));
		interest = getApplicableInterest(waterCharge, noOfDays, timeBasedExmeptionMasterMap.get(SWCalculationConstant.SW_INTEREST_MASTER));
		estimates.put(SWCalculationConstant.SW_TIME_PENALTY, penalty.setScale(2, 2));
		estimates.put(SWCalculationConstant.SW_TIME_INTEREST, interest.setScale(2, 2));
		return estimates;
	}
	
	
	/**
	 * 
	 * @param sewerageCharge
	 * @param noOfDays
	 * @param config
	 * @return
	 */
	public BigDecimal getApplicablePenalty(BigDecimal sewerageCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicablePenalty = BigDecimal.ZERO;
		Map<String, Object> penaltyMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		if (null == penaltyMaster) return applicablePenalty;
		BigDecimal daysApplicable = null != penaltyMaster.get(SWCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(SWCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicablePenalty;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicablePenalty;
		}
		BigDecimal rate = null != penaltyMaster.get(SWCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(SWCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != penaltyMaster.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) penaltyMaster.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicablePenalty = flatAmt.compareTo(sewerageCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else if (rate != null) {
			// rate of penalty
			applicablePenalty = sewerageCharge.multiply(rate.divide(SWCalculationConstant.HUNDRED));
		}
		return applicablePenalty;
	}
	
	/**
	 * 
	 * @param sewerageCharge
	 * @param noOfDays
	 * @param config
	 * @return
	 */
	public BigDecimal getApplicableInterest(BigDecimal sewerageCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicableInterest = BigDecimal.ZERO;
		Map<String, Object> interestMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		if (null == interestMaster) return applicableInterest;
		BigDecimal daysApplicable = null != interestMaster.get(SWCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(SWCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicableInterest;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicableInterest;
		}
		BigDecimal rate = null != interestMaster.get(SWCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(SWCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != interestMaster.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) interestMaster.get(SWCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicableInterest = flatAmt.compareTo(sewerageCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else if (rate != null) {
			// rate of interest
			applicableInterest = sewerageCharge.multiply(rate.divide(SWCalculationConstant.HUNDRED));
		}
		//applicableInterest.multiply(noOfDays.divide(BigDecimal.valueOf(365), 6, 5));
		return applicableInterest;
	}
	
	public Long convertDaysToMilliSecond(int days) {
		return TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS); //gives 86400000
	}
}
