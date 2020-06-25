package org.egov.waterconnection.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterconnection.model.SearchCriteria;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;

public interface WaterDao {
	public void saveWaterConnection(WaterConnectionRequest waterConnectionRequest);

	public List<WaterConnection> getWaterConnectionList(SearchCriteria criteria,RequestInfo requestInfo);
	
	public void updateWaterConnection(WaterConnectionRequest waterConnectionRequest, boolean isStateUpdatable);
}
