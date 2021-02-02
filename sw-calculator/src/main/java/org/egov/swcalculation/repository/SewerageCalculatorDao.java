package org.egov.swcalculation.repository;

import java.util.List;

import org.egov.swcalculation.web.models.SewerageDetails;

public interface SewerageCalculatorDao {

	List<String> getTenantId();
	
	List<SewerageDetails> getConnectionsNoList(String tenantId, String connectionType);
	
}
