package org.egov.waterconnection.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.Calculation;
import org.egov.waterconnection.model.CalculationCriteria;
import org.egov.waterconnection.model.CalculationReq;
import org.egov.waterconnection.model.CalculationRes;
import org.egov.waterconnection.model.Property;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.egov.waterconnection.repository.WaterDaoImpl;
import org.egov.waterconnection.util.WaterServicesUtil;
import org.egov.waterconnection.validator.ValidateProperty;
import org.egov.waterconnection.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@Service
@Slf4j
public class PdfFileStoreService {

	@Autowired
	private ServiceRequestRepository serviceRequestRepository;

	@Autowired
	private WaterServicesUtil waterServiceUtil;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private WSConfiguration config;

	@Autowired
	private WaterDaoImpl waterDao;

	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private ValidateProperty validateProperty;

	String tenantIdReplacer = "$tenantId";
	String fileStoreIdsReplacer = "$.filestoreIds";
	String urlReplacer = "url";
	String requestInfoReplacer = "RequestInfo";
	String WaterConnectionReplacer = "WnsConnection";
	String fileStoreIdReplacer = "$fileStoreIds";
	String totalAmount = "totalAmount";
	String applicationFee = "applicationFee";
	String serviceFee = "serviceFee";
	String tax = "tax";
	String pdfTaxhead = "pdfTaxhead";
	String pdfApplicationKey = "$applicationkey";
	String sla = "sla";
	String slaDate = "slaDate";
	String sanctionLetterDate = "sanctionLetterDate";
	String tenantName = "tenantName";
	String service = "service";
	String propertyKey = "property";
	

	/**
	 * Get fileStroe Id's
	 * 
	 * @param waterConnection
	 * @param requestInfo
	 * @return file store id
	 */
	public String getFileStroeId(WaterConnectionRequest waterConnectionRequest, Property property, String applicationKey) {
		CalculationCriteria criteria = CalculationCriteria.builder().applicationNo(waterConnectionRequest.getWaterConnection().getApplicationNo())
				.waterConnection(waterConnectionRequest.getWaterConnection()).tenantId(property.getTenantId()).build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(waterConnectionRequest.getRequestInfo()).isconnectionCalculation(false).build();
		String applicationStatus = workflowService.getApplicationStatus(waterConnectionRequest.getRequestInfo(),
				waterConnectionRequest.getWaterConnection().getApplicationNo(),
				waterConnectionRequest.getWaterConnection().getTenantId());
		try {
			Object response = serviceRequestRepository.fetchResult(waterServiceUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject waterobject = mapper.convertValue(waterConnectionRequest.getWaterConnection(), JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			
			Optional<Calculation> calculationList = calResponse.getCalculation().stream().findFirst();
			if(calculationList.isPresent()) {
				Calculation cal = calculationList.get();
				waterobject.put(totalAmount, cal.getTotalAmount());
				waterobject.put(applicationFee, cal.getFee());
				waterobject.put(serviceFee, cal.getCharge());
				waterobject.put(tax, cal.getTaxAmount());
				cal.getTaxHeadEstimates().forEach(item -> {
					//We need to remove WS_ --> So that PDF configuration refers the common for both Water & Sewerage
					item.setTaxHeadCode(item.getTaxHeadCode().substring(3));
				});
				waterobject.put(pdfTaxhead, cal);
			}
			waterobject.put(sanctionLetterDate, System.currentTimeMillis());
			BigDecimal slaDays = workflowService.getSlaForState(waterConnectionRequest.getRequestInfo().getUserInfo().getTenantId(), waterConnectionRequest.getRequestInfo(),applicationStatus);
			waterobject.put(sla, slaDays.divide(BigDecimal.valueOf(WCConstants.DAYS_CONST)));
			waterobject.put(slaDate, slaDays.add(new BigDecimal(System.currentTimeMillis())));
			String[] tenantDetails = property.getTenantId().split("\\."); 
			String tenantId = tenantDetails[0];
			if(tenantDetails.length > 1)
			{
				waterobject.put(tenantName, tenantDetails[1].toUpperCase());
			}
			waterobject.put(propertyKey, property);
			waterobject.put(service, "WATER");
			return getFielStoreIdFromPDFService(waterobject, waterConnectionRequest.getRequestInfo(), tenantId, applicationKey);
		} catch (Exception ex) {
			log.error("Calculation response error!!", ex);
			throw new CustomException("WATER_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
		}
	}

	/**
	 * Get file store id from PDF service
	 * 
	 * @param waterobject
	 * @param requestInfo
	 * @param tenantId
	 * @return file store id
	 */
	private String getFielStoreIdFromPDFService(JSONObject waterobject, RequestInfo requestInfo, String tenantId,
			String applicationKey) {
		JSONArray waterconnectionlist = new JSONArray();
		waterconnectionlist.add(waterobject);
		JSONObject requestPayload = new JSONObject();
		requestPayload.put(requestInfoReplacer, requestInfo);
		requestPayload.put(WaterConnectionReplacer, waterconnectionlist);
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(config.getPdfServiceHost());
			String pdfLink = config.getPdfServiceLink();
			pdfLink = pdfLink.replace(tenantIdReplacer, tenantId).replace(pdfApplicationKey, applicationKey);
			builder.append(pdfLink);
			Object response = serviceRequestRepository.fetchResult(builder, requestPayload);
			DocumentContext responseContext = JsonPath.parse(response);
			List<Object> fileStoreIds = responseContext.read("$.filestoreIds");
			if (CollectionUtils.isEmpty(fileStoreIds)) {
				throw new CustomException("EMPTY_FILESTORE_IDS_FROM_PDF_SERVICE",
						"NO file store id found from pdf service");
			}
			return fileStoreIds.get(0).toString();
		} catch (Exception ex) {
			log.error("PDF file store id response error!!", ex);
			throw new CustomException("WATER_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}

	@SuppressWarnings("unchecked")
	public void process(WaterConnectionRequest waterConnectionRequest, String topic) {

		Property property = validateProperty.getOrValidateProperty(waterConnectionRequest);

		HashMap<String, Object> addDetail = mapper
				.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
		if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(WCConstants.APPROVE_CONNECTION_CONST)
				&& addDetail.getOrDefault(WCConstants.ESTIMATION_FILESTORE_ID, null) == null) {
			addDetail.put(WCConstants.ESTIMATION_FILESTORE_ID,
					getFileStroeId(waterConnectionRequest, property, WCConstants.PDF_ESTIMATION_KEY));
		}
		if (waterConnectionRequest.getWaterConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(WCConstants.ACTION_PAY)
				&& addDetail.getOrDefault(WCConstants.SANCTION_LETTER_FILESTORE_ID, null) == null) {
			addDetail.put(WCConstants.SANCTION_LETTER_FILESTORE_ID,
					getFileStroeId(waterConnectionRequest, property, WCConstants.PDF_SANCTION_KEY));
		}
		waterConnectionRequest.getWaterConnection().setAdditionalDetails(addDetail);
		waterDao.saveFileStoreIds(waterConnectionRequest);
	}
}
