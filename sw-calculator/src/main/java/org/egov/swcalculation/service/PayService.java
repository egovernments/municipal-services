package org.egov.swcalculation.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.egov.swcalculation.constants.SWCalculationConstant;
import org.egov.swcalculation.model.TaxHeadEstimate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONArray;

@Service
public class PayService {

	@Autowired
	MasterDataService mDService;
	
	@Autowired
	EstimationService estimationService;
		
	
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
	 * Returns the Amount of penalty that has to be applied on the given tax amount for the given period
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @return
	 */

	
	
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
	 * Overloaded method
	 * Sets the date in to calendar based on the month and date value present in the time array*
	 * @param time
	 * @param cal
	 */
	private void setDateToCalendar(String[] time, Calendar cal) {

		cal.clear();
		TimeZone timeZone = TimeZone.getTimeZone("Asia/Kolkata");
		cal.setTimeZone(timeZone);
		Integer day = Integer.valueOf(time[0]);
		Integer month = Integer.valueOf(time[1])-1;
		// One is subtracted because calender reads january as 0
		Integer year = Integer.valueOf(time[2]);
		cal.set(year, month, day);
	}
	
//	/**
//	 * Fetch the fromFY and take the starting year of financialYear
//	 * calculate the difference between the start of assessment financial year and fromFY
//	 * Add the difference in year to the year in the starting day
//	 * eg: Assessment year = 2017-18 and interestMap fetched from master due to fallback have fromFY = 2015-16
//	 * and startingDay = 01/04/2016. Then diff = 2017-2015 = 2
//	 * Therefore the starting day will be modified from 01/04/2016 to 01/04/2018
//	 * @param assessmentYear Year of the assessment
//	 * @param interestMap The applicable master data
//	 * @return list of string with 0'th element as day, 1'st as month and 2'nd as year
//	 */
//	private String[] getStartTime(String assessmentYear,Map<String, Object> interestMap){
//		String financialYearOfApplicableEntry = ((String) interestMap.get(SWCalculationConstant.FROMFY_FIELD_NAME)).split("-")[0];
//		Integer diffInYear = Integer.valueOf(assessmentYear.split("-")[0]) - Integer.valueOf(financialYearOfApplicableEntry);
//		String startDay = ((String) interestMap.get(SWCalculationConstant.STARTING_DATE_APPLICABLES));
//		Integer yearOfStartDayInApplicableEntry = Integer.valueOf((startDay.split("/")[2]));
//		startDay = startDay.replace(String.valueOf(yearOfStartDayInApplicableEntry),String.valueOf(yearOfStartDayInApplicableEntry+diffInYear));
//		String[] time = startDay.split("/");
//		return time;
//	}
	
	
	/**
	 * 
	 * @param creditAmount
	 * @param debitAmount
	 * @return TaxHead for SW round off
	 */
	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount, boolean isConnectionFee) {

		BigDecimal result = creditAmount.add(debitAmount);
		
		BigDecimal midValue = BigDecimal.valueOf(0.5);
		
		BigDecimal remainder = result.remainder(BigDecimal.ONE);
		
		BigDecimal roundOff = BigDecimal.ZERO;
		
		String taxHead = isConnectionFee == true ? SWCalculationConstant.SW_Round_Off : SWCalculationConstant.SW_ONE_TIME_FEE_ROUND_OFF;
		/*
		 * If the decimal amount is greater than 0.5 we subtract it from 1 and
		 * put it as roundOff taxHead so as to nullify the decimal eg: If the
		 * tax is 12.64 we will add extra tax roundOff taxHead of 0.36 so that
		 * the total becomes 13
		 */
		if(remainder.compareTo(midValue) > 0)
			roundOff =  BigDecimal.ONE.subtract(remainder);
		
		/*
		 * If the decimal amount is less than 0.5 we put negative of it as
		 * roundOff taxHead so as to nullify the decimal eg: If the tax is 12.36
		 * we will add extra tax roundOff taxHead of -0.36 so that the total
		 * becomes 12
		 */
		if(remainder.compareTo(midValue) < 0)
			roundOff = remainder.negate();
		
		if(roundOff.compareTo(BigDecimal.ZERO) != 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOff).taxHeadCode(taxHead)
					.build();
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
			return null;
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
