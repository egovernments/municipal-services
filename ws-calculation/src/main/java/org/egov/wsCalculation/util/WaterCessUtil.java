package org.egov.wsCalculation.util;

import java.math.BigDecimal;
import java.util.List;

import org.egov.waterConnection.model.WaterConnection;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WaterCessUtil {

	public BigDecimal getWaterCess(BigDecimal taxBalAmount, String assesmentYear, List<Object> masterList,
			WaterConnection connection) {
		BigDecimal waterCess = BigDecimal.ZERO;
		return waterCess;
	}
}
