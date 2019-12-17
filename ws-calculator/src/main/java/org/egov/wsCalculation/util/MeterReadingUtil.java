package org.egov.wsCalculation.util;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.mdms.model.MasterDetail;
import org.egov.mdms.model.MdmsCriteria;
import org.egov.mdms.model.MdmsCriteriaReq;
import org.egov.mdms.model.ModuleDetail;
import org.egov.tracer.model.CustomException;
import org.egov.wsCalculation.config.WSCalculationConfiguration;
import org.egov.wsCalculation.model.MeterConnectionRequest;
import org.egov.wsCalculation.model.MeterReading;
import org.egov.wsCalculation.model.MeterReadingResponse;
import org.egov.wsCalculation.repository.ServiceRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MeterReadingUtil {

	private ServiceRequestRepository serviceRequestRepository;
	
	@Autowired
	WSCalculationConfiguration config;


	@Autowired
	public MeterReadingUtil(ServiceRequestRepository serviceRequestRepository) {
		this.serviceRequestRepository = serviceRequestRepository;

	}

	public MeterConnectionRequest getMeterReadingRequest(RequestInfo requestInfo, MeterReading meterReading) {
		MeterConnectionRequest meterConnectionRequest = MeterConnectionRequest.builder().requestInfo(requestInfo)
				.meterReading(meterReading).build();
		return meterConnectionRequest;
	}

	public StringBuilder getDemandGenerationCreateURL() {
		return new StringBuilder().append(config.getBillingServiceHost()).append(config.getDemandCreateEndPoint());
	}

	public List<MeterReading> getMeterReadingDetails(Object result) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			MeterReadingResponse meterReadingResponse = mapper.convertValue(result, MeterReadingResponse.class);
			return meterReadingResponse.getMeterReadings();
		} catch (Exception ex) {
			throw new CustomException("PARSING ERROR", "The property json cannot be parsed");
		}
	}
	
	
	public MdmsCriteriaReq prepareMdMsRequest(String tenantId, String moduleName, List<String> names, String filter,
			RequestInfo requestInfo) {
		List<MasterDetail> masterDetails = new ArrayList<>();
		names.forEach(name -> {
			masterDetails.add(MasterDetail.builder().name(name).filter(filter).build());
		});
		ModuleDetail moduleDetail = ModuleDetail.builder().moduleName(moduleName).masterDetails(masterDetails).build();
		List<ModuleDetail> moduleDetails = new ArrayList<>();
		moduleDetails.add(moduleDetail);
		MdmsCriteria mdmsCriteria = MdmsCriteria.builder().tenantId(tenantId).moduleDetails(moduleDetails).build();
		return MdmsCriteriaReq.builder().requestInfo(requestInfo).mdmsCriteria(mdmsCriteria).build();
	}

}
