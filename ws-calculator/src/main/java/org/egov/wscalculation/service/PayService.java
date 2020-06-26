package org.egov.wscalculation.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.egov.wscalculation.constants.WSCalculationConstant;
import org.egov.wscalculation.model.TaxHeadEstimate;
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
	 * Decimal is ceiled for all the tax heads
	 * 
	 * if the decimal is greater than 0.5 upper bound will be applied
	 * 
	 * else if decimal is lesser than 0.5 lower bound is applied
	 * 
	 */

	public TaxHeadEstimate roundOfDecimals(BigDecimal creditAmount, BigDecimal debitAmount, boolean isConnectionFee) {
		BigDecimal roundOffPos = BigDecimal.ZERO;
		BigDecimal roundOffNeg = BigDecimal.ZERO;
        String taxHead = isConnectionFee == true ? WSCalculationConstant.WS_Round_Off : WSCalculationConstant.WS_ONE_TIME_FEE_ROUND_OFF;
		BigDecimal result = creditAmount.add(debitAmount);
		BigDecimal roundOffAmount = result.setScale(2, 2);
		BigDecimal reminder = roundOffAmount.remainder(BigDecimal.ONE);

		if (reminder.doubleValue() >= 0.5)
			roundOffPos = roundOffPos.add(BigDecimal.ONE.subtract(reminder));
		else if (reminder.doubleValue() < 0.5)
			roundOffNeg = roundOffNeg.add(reminder).negate();

		if (roundOffPos.doubleValue() > 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffPos).taxHeadCode(taxHead)
					.build();
		else if (roundOffNeg.doubleValue() < 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffNeg).taxHeadCode(taxHead)
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
		penalty = getApplicablePenalty(waterCharge, noOfDays, timeBasedExmeptionMasterMap.get(WSCalculationConstant.WC_PENANLTY_MASTER));
		interest = getApplicableInterest(waterCharge, noOfDays, timeBasedExmeptionMasterMap.get(WSCalculationConstant.WC_INTEREST_MASTER));
		estimates.put(WSCalculationConstant.WS_TIME_PENALTY, penalty.setScale(2, 2));
		estimates.put(WSCalculationConstant.WS_TIME_INTEREST, interest.setScale(2, 2));
		return estimates;
	}

	/**
	 * Returns the Amount of penalty that has to be applied on the given tax amount for the given period
	 * 
	 * @param taxAmt
	 * @param assessmentYear
	 * @return applicable penalty for given time
	 */
	public BigDecimal getPenalty(BigDecimal taxAmt, String assessmentYear, JSONArray penaltyMasterList, BigDecimal noOfDays) {

		BigDecimal penaltyAmt = BigDecimal.ZERO;
		Map<String, Object> penalty = mDService.getApplicableMaster(assessmentYear, penaltyMasterList);
		if (null == penalty) return penaltyAmt;
			penaltyAmt = mDService.calculateApplicables(taxAmt, penalty);
		return penaltyAmt;
	}


	/**
	 * Returns the current end of the day epoch time for the given epoch time
	 * @param epoch The epoch time for which end of day time is required
	 * @return End of day epoch time for the given time
	 */
	public static Long getEODEpoch(Long epoch){
		LocalDate date =
				Instant.ofEpochMilli(epoch).atZone(ZoneId.of(ZoneId.SHORT_IDS.get("IST"))).toLocalDate();
		LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
		Long eodEpoch = endOfDay.atZone(ZoneId.of(ZoneId.SHORT_IDS.get("IST"))).toInstant().toEpochMilli();
		return eodEpoch;
	}
	
	/**
	 * 
	 * @param waterCharge
	 * @param noOfDays
	 * @param config
	 *            master configuration
	 * @return applicable penalty
	 */
	public BigDecimal getApplicablePenalty(BigDecimal waterCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicablePenalty = BigDecimal.ZERO;
		Map<String, Object> penaltyMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		if (null == penaltyMaster) return applicablePenalty;
		BigDecimal daysApplicable = null != penaltyMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicablePenalty;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicablePenalty;
		}
		BigDecimal rate = null != penaltyMaster.get(WSCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != penaltyMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) penaltyMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicablePenalty = flatAmt.compareTo(waterCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else {
			// rate of penalty
			applicablePenalty = waterCharge.multiply(rate.divide(WSCalculationConstant.HUNDRED));
		}
		return applicablePenalty;
	}
	
	/**
	 * 
	 * @param waterCharge
	 * @param noOfDays
	 * @param config
	 *            master configuration
	 * @return applicable Interest
	 */
	public BigDecimal getApplicableInterest(BigDecimal waterCharge, BigDecimal noOfDays, JSONArray config) {
		BigDecimal applicableInterest = BigDecimal.ZERO;
		Map<String, Object> interestMaster = mDService.getApplicableMaster(estimationService.getAssessmentYear(), config);
		if (null == interestMaster) return applicableInterest;
		BigDecimal daysApplicable = null != interestMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(WSCalculationConstant.DAYA_APPLICABLE_NAME)).intValue())
				: null;
		if (daysApplicable == null)
			return applicableInterest;
		BigDecimal daysDiff = noOfDays.subtract(daysApplicable);
		if (daysDiff.compareTo(BigDecimal.ONE) < 0) {
			return applicableInterest;
		}
		BigDecimal rate = null != interestMaster.get(WSCalculationConstant.RATE_FIELD_NAME)
				? BigDecimal.valueOf(((Number) interestMaster.get(WSCalculationConstant.RATE_FIELD_NAME)).doubleValue())
				: null;

		BigDecimal flatAmt = null != interestMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)
				? BigDecimal
						.valueOf(((Number) interestMaster.get(WSCalculationConstant.FLAT_AMOUNT_FIELD_NAME)).doubleValue())
				: BigDecimal.ZERO;

		if (rate == null)
			applicableInterest = flatAmt.compareTo(waterCharge) > 0 ? BigDecimal.ZERO : flatAmt;
		else{
			// rate of interest
			applicableInterest = waterCharge.multiply(rate.divide(WSCalculationConstant.HUNDRED));
		}
		//applicableInterest.multiply(noOfDays.divide(BigDecimal.valueOf(365), 6, 5));
		return applicableInterest;
	}
	
	public Long convertDaysToMilliSecond(int days) {
		return TimeUnit.MILLISECONDS.convert(days, TimeUnit.DAYS); //gives 86400000
	}
}
