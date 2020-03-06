package org.egov.swcalculation.repository;

import java.util.List;

public interface SewerageCalculatorDao {

	public List<String> getTenantId();
	
	public List<String> getConnectionsNoList(String tenantId, String connectionType);
	
}
