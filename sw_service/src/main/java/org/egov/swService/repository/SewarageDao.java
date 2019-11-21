package org.egov.swService.repository;

import java.util.List;
import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.SearchCriteria;

public interface SewarageDao {
	public void saveSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public List<SewerageConnection> getSewerageConnectionList(SearchCriteria criteria,
			RequestInfo requestInfo);

	public void updatSewerageConnection(SewerageConnectionRequest sewerageConnectionRequest);

	public int isSewerageConnectionExist(List<String> ids);

}
