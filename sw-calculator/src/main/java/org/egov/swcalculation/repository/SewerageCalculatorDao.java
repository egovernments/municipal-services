package org.egov.swcalculation.repository;

import java.util.List;

public interface SewerageCalculatorDao {

	List<String> getTenantId();
	
	List<String> getConnectionsNoList(String tenantId, String connectionType, Integer batchOffset, Integer batchsize);

	long getConnectionCount(String tenantid);
	
}
