package org.egov.gcservice.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.gcservice.web.models.SearchCriteria;
import org.egov.gcservice.web.models.GarbageConnection;
import org.egov.gcservice.web.models.GarbageConnectionRequest;

public interface GarbageService {

	List<GarbageConnection> createGarbageConnection(GarbageConnectionRequest sewarageConnectionRequest);

	List<GarbageConnection> search(SearchCriteria criteria, RequestInfo requestInfo);

	List<GarbageConnection> updateGarbageConnection(GarbageConnectionRequest sewarageConnectionRequest);

}
