package org.egov.wsCalculation.service;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.Idgen.IdResponse;
import org.egov.wsCalculation.repository.IdGenRepository;
import org.egov.wscalculation.config.WSCalculationConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class EnrichmentService {

	private IdGenRepository idGenRepository;
	private WSCalculationConfiguration config;

	@Autowired
	public EnrichmentService(IdGenRepository idGenRepository, WSCalculationConfiguration config) {
		this.idGenRepository = idGenRepository;
		this.config = config;
	}

	private List<String> getIdList(RequestInfo requestInfo, String tenantId, String idKey, String idformat, int count) {
		List<IdResponse> idResponses = idGenRepository.getId(requestInfo, tenantId, idKey, idformat, count)
				.getIdResponses();

		if (CollectionUtils.isEmpty(idResponses))
			throw new CustomException("IDGEN ERROR", "No ids returned from idgen Service");

		return idResponses.stream().map(IdResponse::getId).collect(Collectors.toList());
	}

	/**
	 * Sets the ApplicationNumber for given MeterConnectionRequest
	 *
	 * @param request
	 *            MeterConnectionRequest which is to be created
	 */
	private void setIdgenIds(MeterConnectionRequest meterConnectionRequest) {
		RequestInfo requestInfo = meterConnectionRequest.getRequestInfo();
		String tenantId = meterConnectionRequest.getRequestInfo().getUserInfo().getTenantId();
		MeterReading meterReading = meterConnectionRequest.getMeterReading();

		List<String> applicationNumbers = getIdList(requestInfo, tenantId, config.getApplicationNumberIdgenName(),
				config.getApplicationNumberIdgenFormat(), 1);
		ListIterator<String> itr = applicationNumbers.listIterator();

		Map<String, String> errorMap = new HashMap<>();
		if (applicationNumbers.size() != 1) {
			errorMap.put("IDGEN ERROR ",
					"The number of LicenseNumber returned by idgen is not equal to number of TradeLicenses");
		}

		if (!errorMap.isEmpty())
			throw new CustomException(errorMap);
		meterReading.setId(itr.next());

	}
	
	 /**
     * Enriches the incoming createRequest
     * @param meterConnectionRequest The create request for the meter reading
     */
	
	public void enrichMeterReadingRequest(MeterConnectionRequest meterConnectionRequest){
		
		setIdgenIds(meterConnectionRequest);
		
	}

}
