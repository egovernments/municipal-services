package org.egov.waterConnection.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.SearchCriteria;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;

public interface WaterDao {
	public void saveWaterConnection(WaterConnectionRequest waterConnectionRequest);

	public List<WaterConnection> getWaterConnectionList(SearchCriteria criteria,RequestInfo requestInfo);
	
	public void updateWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isStateUpdatable);
}
