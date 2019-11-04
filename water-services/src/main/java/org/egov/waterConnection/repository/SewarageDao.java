package org.egov.waterConnection.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterConnection.model.SewerageConnection;
import org.egov.waterConnection.model.SewerageConnectionRequest;
import org.egov.waterConnection.model.WaterConnectionSearchCriteria;

public interface SewarageDao {
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public List<SewerageConnection> getSewerageConnectionList(WaterConnectionSearchCriteria criteria,
			RequestInfo requestInfo);

	public void updatSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public int isSewerageConnectionExist(List<String> ids);

}
