package org.egov.swservice.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.model.SearchCriteria;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;

public interface SewarageService {

	public List<SewerageConnection> createSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest);

	public List<SewerageConnection> search(SearchCriteria criteria, RequestInfo requestInfo);

	public List<SewerageConnection> updateSewarageConnection(SewerageConnectionRequest sewarageConnectionRequest);

}
