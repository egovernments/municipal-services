package org.egov.waterconnection.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.egov.waterconnection.config.WSConfiguration;
import org.egov.waterconnection.constants.WCConstants;
import org.egov.waterconnection.model.CalculationCriteria;
import org.egov.waterconnection.model.CalculationReq;
import org.egov.waterconnection.model.CalculationRes;
import org.egov.waterconnection.model.WaterConnection;
import org.egov.waterconnection.model.WaterConnectionRequest;
import org.egov.waterconnection.repository.ServiceRequestRepository;
import org.egov.waterconnection.repository.WaterDaoImpl;
import org.egov.waterconnection.util.WaterServicesUtil;
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

	/**
	 * Get fileStroe Id's
	 * 
	 * @param waterConnection
	 * @param requestInfo
	 * @return file store id
	 */
	public String getFileStroeId(WaterConnection waterConnection, RequestInfo requestInfo, String applicationKey) {
		CalculationCriteria criteria = CalculationCriteria.builder().applicationNo(waterConnection.getApplicationNo())
				.waterConnection(waterConnection).tenantId(waterConnection.getProperty().getTenantId()).build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(requestInfo).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(waterServiceUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject waterobject = mapper.convertValue(waterConnection, JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			waterobject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			waterobject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			waterobject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			waterobject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			waterobject.put(pdfTaxhead, calResponse.getCalculation().get(0).getTaxHeadEstimates());
			BigDecimal slaDays = workflowService.getSlaForState(requestInfo.getUserInfo().getTenantId(), requestInfo,
					waterConnection.getApplicationStatus().name());
			waterobject.put(sla, slaDays.divide(BigDecimal.valueOf(WCConstants.DAYS_CONST)));
			waterobject.put(slaDate, slaDays.add(new BigDecimal(waterConnection.getConnectionExecutionDate())));
			String tenantId = waterConnection.getProperty().getTenantId().split("\\.")[0];
			return getFielStoreIdFromPDFService(waterobject, requestInfo, tenantId, applicationKey);
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
		HashMap<String, Object> addDetail = mapper
				.convertValue(waterConnectionRequest.getWaterConnection().getAdditionalDetails(), HashMap.class);
		if (addDetail.getOrDefault(WCConstants.ESTIMATION_FILESTORE_ID, null) == null) {
			addDetail.put(WCConstants.ESTIMATION_FILESTORE_ID,
					getFileStroeId(waterConnectionRequest.getWaterConnection(), waterConnectionRequest.getRequestInfo(),
							WCConstants.PDF_ESTIMATION_KEY));
		}
		if (addDetail.getOrDefault(WCConstants.SANCTION_LETTER_FILESTORE_ID, null) == null) {
			addDetail.put(WCConstants.SANCTION_LETTER_FILESTORE_ID,
					getFileStroeId(waterConnectionRequest.getWaterConnection(), waterConnectionRequest.getRequestInfo(),
							WCConstants.PDF_SANCTION_KEY));
		}
		waterConnectionRequest.getWaterConnection().setAdditionalDetails(addDetail);
		waterDao.saveFileStoreIds(waterConnectionRequest);
	}
}
