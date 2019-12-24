package org.egov.wsCalculation.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;

public interface WSCalculationDao {

	public void saveWaterConnection(MeterConnectionRequest meterConnectionRequest);
	
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria);
	
	public ArrayList<String> searchTenentIds();

	public ArrayList<String> searchConnectionNos(String connectionType, String tenentId);
	
	public List<MeterReading> searchCurrentMeterReadings(MeterReadingSearchCriteria criteria);
	
	public int isMeterReadingConnectionExist(List<String> ids);
	
	public List<String> getConnectionsNoList(String tenantId, String connectionType);
	
	public List<String> getTenantId();

}
