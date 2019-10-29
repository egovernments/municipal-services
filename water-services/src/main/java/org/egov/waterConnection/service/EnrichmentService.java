package org.egov.waterConnection.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.model.Property;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;
import org.egov.waterConnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnrichmentService {

	@Autowired
	WaterServicesUtil waterServicesUtil;

	public void enrichWaterSearch(List<WaterConnection> waterConnectionList, RequestInfo requestInfo) {

		waterConnectionList.forEach(waterConnection -> {
			List<Property> propertyList;
			if (waterConnection.getProperty().getId() == null || waterConnection.getProperty().getId().isEmpty()) {
				throw new CustomException("INVALID PROPERTY ID", "NO ID FOUND FOR PROPERTY");
			}
			if (waterConnection.getProperty().getId() != null) {
				Set<String> propertyIds = new HashSet<>();
				propertyIds.add(waterConnection.getProperty().getId());

				WaterConnectionSearchCriteria waterConnectionSearchCriteria = WaterConnectionSearchCriteria.builder()
						.ids(propertyIds).build();
				propertyList = waterServicesUtil.propertySearchOnCriteria(waterConnectionSearchCriteria, requestInfo);

				waterConnection.setProperty(propertyList.get(0));

			}
		});

	}

	public void enrichWaterConnection(WaterConnectionRequest waterConnectionRequest, List<Property> propertyList) {
		if (propertyList != null && !propertyList.isEmpty())
			waterConnectionRequest.getWaterConnection().setProperty(propertyList.get(0));
	}
}
