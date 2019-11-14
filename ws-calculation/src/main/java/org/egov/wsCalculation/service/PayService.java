package org.egov.wsCalculation.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.wsCalculation.constants.WSCalculationConstant;
import org.egov.wsCalculation.model.TaxHeadEstimate;
import org.egov.wscalculation.config.WSCalculationConfiguration;

import net.minidev.json.JSONArray;

public class PayService {
	
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
					.taxHeadCode(WSCalculationConfiguration.WS_Round_Off).build();
		else if (roundOffNeg.doubleValue() < 0)
			return TaxHeadEstimate.builder().estimateAmount(roundOffNeg)
					.taxHeadCode(WSCalculationConfiguration.WS_Round_Off).build();
		else
			return null;
	}
	
	public Map<String, BigDecimal> applyPenaltyRebateAndInterest(BigDecimal taxAmt, BigDecimal collectedPtTax,
			String assessmentYear, Map<String, JSONArray> timeBasedExmeptionMasterMap) {

		if (BigDecimal.ZERO.compareTo(taxAmt) >= 0)
			return null;
		Map<String, BigDecimal> estimates = new HashMap<>();
		// estimates.put(CalculatorConstants.PT_TIME_REBATE, rebate.setScale(2,
		// 2).negate());
		// estimates.put(CalculatorConstants.PT_TIME_PENALTY, penalty.setScale(2, 2));
		// estimates.put(CalculatorConstants.PT_TIME_INTEREST, interest.setScale(2, 2));
		estimates.put(WSCalculationConfiguration.Water_Time_Rebate, BigDecimal.ZERO);
		estimates.put(WSCalculationConfiguration.Water_Time_PENALTY, BigDecimal.ZERO);
		estimates.put(WSCalculationConfiguration.Water_Time_INTEREST, BigDecimal.ZERO);
		return estimates;
	}

}
