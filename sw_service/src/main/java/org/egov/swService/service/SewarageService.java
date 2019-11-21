package org.egov.swService.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.model.SearchCriteria;

public interface SewarageService {

	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest);

	public List<SewerageConnection> search(SearchCriteria criteria, RequestInfo requestInfo);

	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest);

}
