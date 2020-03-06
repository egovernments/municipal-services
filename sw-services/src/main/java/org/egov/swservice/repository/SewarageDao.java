package org.egov.swservice.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;

public interface SewarageDao {
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public List<SewerageConnection> getSewerageConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo);

	public void updateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isStateUpdatable);

}
