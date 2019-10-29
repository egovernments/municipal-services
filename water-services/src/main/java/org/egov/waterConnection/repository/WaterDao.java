package org.egov.waterConnection.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;

public interface WaterDao {
	public void saveWaterConnection(WaterConnectionRequest waterConnectionRequest);

	public List<WaterConnection> getWaterConnectionList(WaterConnectionSearchCriteria criteria,RequestInfo requestInfo);
	
	public void updateWaterConnection(WaterConnectionRequest waterConnectionRequest);
	
	public int isWaterConnectionExist(List<String> ids);

}
