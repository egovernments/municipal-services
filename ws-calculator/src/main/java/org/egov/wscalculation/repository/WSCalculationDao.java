package org.egov.wscalculation.repository;

import java.util.ArrayList;
import java.util.List;

import org.egov.wscalculation.model.MeterConnectionRequest;
import org.egov.wscalculation.model.MeterReading;
import org.egov.wscalculation.model.MeterReadingSearchCriteria;

public interface WSCalculationDao {

	public void savemeterReading(MeterConnectionRequest meterConnectionRequest);
	
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria);
	
	public ArrayList<String> searchTenentIds();

	public ArrayList<String> searchConnectionNos(String connectionType, String tenentId);
	
	public List<MeterReading> searchCurrentMeterReadings(MeterReadingSearchCriteria criteria);
	
	public int isMeterReadingConnectionExist(List<String> ids);
	
	public List<String> getConnectionsNoList(String tenantId, String connectionType);
	
	public List<String> getTenantId();
	
	public int isBillingPeriodExists(String connectionNo, String billingPeriod);

}
