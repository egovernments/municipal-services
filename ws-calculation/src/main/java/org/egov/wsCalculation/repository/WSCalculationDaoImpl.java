package org.egov.wsCalculation.repository;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.producer.WSCalculationProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class WSCalculationDaoImpl implements WSCalculationDao {
	
	@Autowired
	WSCalculationProducer wSCalculationProducer;
	
	
	@Value("${egov.waterservice.createWaterConnection}")
	private String createMeterConnection;

	@Override
	public void saveWaterConnection(MeterConnectionRequest meterConnectionRequest) {
     wSCalculationProducer.push(createMeterConnection, meterConnectionRequest);
	}

}
