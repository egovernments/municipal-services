package org.egov.waterconnection.service;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.waterconnection.web.models.SearchCriteria;
import org.egov.waterconnection.web.models.WaterConnection;
import org.egov.waterconnection.web.models.WaterConnectionRequest;

public interface WaterService {

	List<WaterConnection> createWaterConnection(WaterConnectionRequest waterConnectionRequest);

	List<WaterConnection> search(SearchCriteria criteria, RequestInfo requestInfo);
	
	List<WaterConnection> searchWaterConnectionPlainSearch(SearchCriteria criteria, RequestInfo requestInfo);
	
	List<WaterConnection> updateWaterConnection(WaterConnectionRequest waterConnectionRequest);

	void disConnectWaterConnection(String connectionNo,RequestInfo requestInfo,String tenantId);

}
