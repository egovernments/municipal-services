package org.egov.swService.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SearchCriteria;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;

public interface SewarageDao {
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public List<SewerageConnection> getSewerageConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo);

	public void updateSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest, boolean isStateUpdatable);

}
