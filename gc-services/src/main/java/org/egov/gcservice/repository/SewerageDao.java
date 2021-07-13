package org.egov.gcservice.repository;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;

public interface SewerageDao {
	void saveGarbageConnection(GarbageConnectionRequest garbageConnectionRequest);

	List<GarbageConnection> getGarbageConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo);

	void updateGarbageConnection(GarbageConnectionRequest garbageConnectionRequest, boolean isStateUpdatable);

}
