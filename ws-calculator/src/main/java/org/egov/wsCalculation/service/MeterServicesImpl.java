package org.egov.wsCalculation.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.wsCalculation.model.CalculationCriteria;
import org.egov.wsCalculation.model.CalculationReq;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingSearchCriteria;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.egov.wsCalculation.repository.WSCalculationDao;
import org.egov.wsCalculation.util.MeterReadingUtil;
import org.egov.wsCalculation.validator.MDMSValidator;
import org.egov.wsCalculation.validator.WSCalculationValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MeterServicesImpl implements MeterService {

	@Autowired
	MeterReadingUtil meterReadingUtil;

	@Autowired
	WSCalculationDao wSCalculationDao;

	@Autowired
	MDMSValidator mDMSValidator;

	@Autowired
	WSCalculationValidator wsCalculationValidator;
	
	@Autowired
	WSCalculationService wSCalculationService;
	
	@Autowired
	EstimationService estimationService;


	private EnrichmentService enrichmentService;

	@Autowired
	public MeterServicesImpl(EnrichmentService enrichmentService) {
		this.enrichmentService = enrichmentService;
	}

	/**
	 * 
	 * @param meterConnectionRequest MeterConnectionRequest contains meter reading connection to be created
	 * @return List of MeterReading after create
	 */

	@Override
	public List<MeterReading> createMeterReading(MeterConnectionRequest meterConnectionRequest) {
		List<MeterReading> meterReadingsList = new ArrayList<MeterReading>();
		wsCalculationValidator.validateMeterReading(meterConnectionRequest, true);
		// mDMSValidator.validateMasterData(meterConnectionRequest);
		enrichmentService.enrichMeterReadingRequest(meterConnectionRequest);
		meterReadingsList.add(meterConnectionRequest.getMeterReading());
		wSCalculationDao.savemeterReading(meterConnectionRequest);
		List<CalculationCriteria> criterias = new ArrayList<>();
		meterReadingsList.forEach(reading -> {
			CalculationCriteria criteria = new CalculationCriteria();
			criteria.setTenantId(meterConnectionRequest.getRequestInfo().getUserInfo().getTenantId());
			criteria.setAssessmentYear(estimationService.getAssessmentYear());
			criteria.setCurrentReading(reading.getCurrentReading());
			criteria.setLastReading(reading.getLastReading());
			criteria.setConnectionNo(reading.getConnectionNo());
		});
		CalculationReq calculationRequest = CalculationReq.builder()
				.requestInfo(meterConnectionRequest.getRequestInfo()).calculationCriteria(criterias).build();
		wSCalculationService.getCalculation(calculationRequest);
		return meterReadingsList;
	}
	
	/**
	 * 
	 * @param meterConnectionSearchCriteria
	 *            MeterConnectionSearchCriteria contains meter reading
	 *            connection criterias to be searched for in the meter
	 *            connection table
	 * @return List of MeterReading after search
	 */


	@Override
	public List<MeterReading> searchMeterReadings(MeterReadingSearchCriteria criteria, RequestInfo requestInfo) {
		wsCalculationValidator.validateMeterReadingSearchCriteria(criteria);
		List<MeterReading> meterReadings = wSCalculationDao.searchMeterReadings(criteria);
		return meterReadings;
	}
}
