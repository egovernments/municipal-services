package org.egov.waterconnection.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.MeterConnectionRequest;
import org.egov.waterconnection.model.MeterReading;
import org.egov.waterconnection.model.MeterReading.MeterStatusEnum;
import org.egov.waterconnection.model.MeterReadingResponse;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MeterReadingService {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private WaterServicesUtil waterServiceUtil;

	@SuppressWarnings("unchecked")
	public void process(WaterConnectionRequest request, String topic) {
		try {
			BigDecimal initialMeterReading = BigDecimal.ZERO;
			if (!StringUtils.isEmpty(request.getWaterConnection().getAdditionalDetails())) {
				HashMap<String, Object> addDetail = mapper
						.convertValue(request.getWaterConnection().getAdditionalDetails(), HashMap.class);
				if (addDetail.getOrDefault(WCConstants.INITIAL_METER_READING_CONST, null) != null) {
					initialMeterReading = new BigDecimal(
							String.valueOf(addDetail.get(WCConstants.INITIAL_METER_READING_CONST)));
					MeterConnectionRequest req = MeterConnectionRequest.builder()
							.meterReading(MeterReading.builder()
									.connectionNo(request.getWaterConnection().getConnectionNo())
									.currentReading(initialMeterReading.doubleValue())
									.currentReadingDate(System.currentTimeMillis()).meterStatus(MeterStatusEnum.WORKING)
									.billingPeriod(getBillingPeriod()).generateDemand(Boolean.FALSE).lastReading(0.0)
									.lastReadingDate(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)).build())
							.requestInfo(request.getRequestInfo()).build();
					Object response = serviceRequestRepository.fetchResult(waterServiceUtil.getMeterReadingCreateURL(),
							req);
					MeterReadingResponse readingResponse = mapper.convertValue(response, MeterReadingResponse.class);
					log.info(mapper.writeValueAsString(readingResponse));
				}
			} else {
				log.info("Intial Meter Reading Not Present!!");
			}
		} catch (Exception ex) {
			log.error("Error while creating meter reading!!!", ex);
		}
	}

	private String getBillingPeriod() {
		LocalDate currentdate = LocalDate.now();
		StringBuilder builder = new StringBuilder();
		return builder.append(currentdate.getMonth().name().substring(0, 3)).append(" - ")
				.append(String.valueOf(currentdate.getYear())).toString();
	}
}
