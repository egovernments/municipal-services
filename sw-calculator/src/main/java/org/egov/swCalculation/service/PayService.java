package org.egov.swCalculation.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.egov.swCalculation.constants.SWCalculationConstant;
import org.egov.swCalculation.model.TaxHeadEstimate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.minidev.json.JSONArray;

@Service
public class PayService {

	@Autowired
	MasterDataService mDService;
	/**
	 * 
	 * @param taxAmt
	 * @param collectedSWTax
	 * @param assessmentYear
	 * @param timeBasedExmeptionMasterMap
	 * @return
	 */
	public Map<String, BigDecimal> applyPenaltyRebateAndInterest(BigDecimal taxAmt, BigDecimal collectedSWTax,
			String assessmentYear, Map<String, JSONArray> timeBasedExmeptionMasterMap) {

		if (BigDecimal.ZERO.compareTo(taxAmt) >= 0)
			return null;
		Map<String, BigDecimal> estimates = new HashMap<>();
		BigDecimal rebate = getRebate(taxAmt, assessmentYear,
				timeBasedExmeptionMasterMap.get(SWCalculationConstant.SW_REBATE_MASTER));
		BigDecimal penalty = BigDecimal.ZERO;
		BigDecimal interest = BigDecimal.ZERO;

		if (rebate.equals(BigDecimal.ZERO)) {
			penalty = getPenalty(taxAmt, assessmentYear,
					timeBasedExmeptionMasterMap.get(SWCalculationConstant.SW_PENANLTY_MASTER));
//			interest = getInterest(taxAmt, assessmentYear,
//					timeBasedExmeptionMasterMap.get(WSCalculationConstant.WC_INTEREST_MASTER), receipts);
		}
		estimates.put(SWCalculationConstant.SW_TIME_REBATE, rebate.setScale(2, 2).negate());
		estimates.put(SWCalculationConstant.SW_TIME_PENALTY, penalty.setScale(2, 2));
		estimates.put(SWCalculationConstant.SW_TIME_INTEREST, BigDecimal.ZERO);
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
	public BigDecimal getRebate(BigDecimal taxAmt, String assessmentYear, JSONArray rebateMasterList) {

		BigDecimal rebateAmt = BigDecimal.ZERO;
		Map<String, Object> rebate = mDService.getApplicableMaster(assessmentYear, rebateMasterList);

		if (null == rebate)
			return rebateAmt;

		String[] time = ((String) rebate.get(SWCalculationConstant.ENDING_DATE_APPLICABLES)).split("/");
		Calendar cal = Calendar.getInstance();
		setDateToCalendar(assessmentYear, time, cal);

		if (cal.getTimeInMillis() > System.currentTimeMillis())
			rebateAmt = mDService.calculateApplicables(taxAmt, rebate);

		return rebateAmt;
	}
	
	
	/**
	 * Returns the Amount of penalty that has to be applied on the given tax amount for the given period
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @return
	 */
	public BigDecimal getPenalty(BigDecimal taxAmt, String assessmentYear, JSONArray penaltyMasterList) {

		BigDecimal penaltyAmt = BigDecimal.ZERO;
		Map<String, Object> penalty = mDService.getApplicableMaster(assessmentYear, penaltyMasterList);
		if (null == penalty) return penaltyAmt;

		String[] time = getStartTime(assessmentYear,penalty);
		Calendar cal = Calendar.getInstance();
		setDateToCalendar(time, cal);
		Long currentIST = System.currentTimeMillis()+SWCalculationConstant.TIMEZONE_OFFSET;

		if (cal.getTimeInMillis() < currentIST)
			penaltyAmt = mDService.calculateApplicables(taxAmt, penalty);

		return penaltyAmt;
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
	
	/**
	 * Fetch the fromFY and take the starting year of financialYear
	 * calculate the difference between the start of assessment financial year and fromFY
	 * Add the difference in year to the year in the starting day
	 * eg: Assessment year = 2017-18 and interestMap fetched from master due to fallback have fromFY = 2015-16
	 * and startingDay = 01/04/2016. Then diff = 2017-2015 = 2
	 * Therefore the starting day will be modified from 01/04/2016 to 01/04/2018
	 * @param assessmentYear Year of the assessment
	 * @param interestMap The applicable master data
	 * @return list of string with 0'th element as day, 1'st as month and 2'nd as year
	 */
	private String[] getStartTime(String assessmentYear,Map<String, Object> interestMap){
		String financialYearOfApplicableEntry = ((String) interestMap.get(SWCalculationConstant.FROMFY_FIELD_NAME)).split("-")[0];
		Integer diffInYear = Integer.valueOf(assessmentYear.split("-")[0]) - Integer.valueOf(financialYearOfApplicableEntry);
		String startDay = ((String) interestMap.get(SWCalculationConstant.STARTING_DATE_APPLICABLES));
		Integer yearOfStartDayInApplicableEntry = Integer.valueOf((startDay.split("/")[2]));
		startDay = startDay.replace(String.valueOf(yearOfStartDayInApplicableEntry),String.valueOf(yearOfStartDayInApplicableEntry+diffInYear));
		String[] time = startDay.split("/");
		return time;
	}
	
	
	/**
	 * 
	 * @param creditAmount
	 * @param debitAmount
	 * @return TaxHead for SW round off
	 */
	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount) {

		BigDecimal result = creditAmount.add(debitAmount);
		
		BigDecimal midValue = new BigDecimal(0.5);
		
		BigDecimal remainder = result.remainder(BigDecimal.ONE);
		
		BigDecimal roundOff = BigDecimal.ZERO;
		
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
			return TaxHeadEstimate.builder().estimateAmount(roundOff).taxHeadCode(SWCalculationConstant.SW_Round_Off)
					.build();
		else
			return null;
	}
}
