package org.egov.waterconnection.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterconnection.model.*;

public interface WaterService {

	public List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest);

	public List<WaterConnection> search(SearchCriteria criteria, RequestInfo requestInfo);
	
	public List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest);

	public DemoModel getDemoFunction(DemoInput demoInput);

}
