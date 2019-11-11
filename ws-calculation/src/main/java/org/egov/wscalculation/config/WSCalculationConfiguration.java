package org.egov.wscalculation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class WSCalculationConfiguration {

	@Value("${egov.ws_calculation.meterReading.default.limit}")
	private Integer meterReadingDefaultLimit;

	@Value("${egov.ws_calculation.meterReading.default.offset}")
	private Integer meterReadingDefaultOffset;

	// Idgen Config
	@Value("${egov.idgen.host}")
	private String idGenHost;

	@Value("${egov.idgen.path}")
	private String idGenPath;

	@Value("${egov.idgen.mr.applicationNum.name}")
	private String applicationNumberIdgenName;

	@Value("${egov.idgen.mr.applicationNum.format}")
	private String applicationNumberIdgenFormat;

}
