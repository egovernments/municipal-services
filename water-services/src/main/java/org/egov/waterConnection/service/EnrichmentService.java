package org.egov.waterConnection.service;

import java.util.List;

import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.WaterConnection;
import org.springframework.stereotype.Service;

@Service
public class EnrichmentService {

	public void enrichWaterSearch(List<WaterConnection> waterConnectionList) {
		waterConnectionList.forEach(waterConnection -> {
			if(waterConnection.getProperty().getId() == null || waterConnection.getProperty().getId().isEmpty()) {
				throw new CustomException("INVALID PROPERTY ID", "NO ID FOUND FOR PROPERTY");
			}
			
		});
	}
}
