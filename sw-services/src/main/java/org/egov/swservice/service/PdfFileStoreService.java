package org.egov.swservice.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.CalculationCriteria;
import org.egov.swservice.model.CalculationReq;
import org.egov.swservice.model.CalculationRes;
import org.egov.swservice.model.SewerageConnection;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.repository.SewarageDaoImpl;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.swservice.workflow.WorkflowService;
import org.egov.tracer.model.CustomException;
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
	private SewerageServicesUtil sewerageServiceUtil;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private SWConfiguration config;

	@Autowired
	private SewarageDaoImpl sewerageDao;

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
	 * @param sewerageConnection
	 * @param requestInfo
	 * @param applicationKey
	 * @return file store id
	 */
	public String getFileStroeId(SewerageConnection sewerageConnection, RequestInfo requestInfo,
			String applicationKey) {
		CalculationCriteria criteria = CalculationCriteria.builder()
				.applicationNo(sewerageConnection.getApplicationNo()).sewerageConnection(sewerageConnection)
				.tenantId(sewerageConnection.getProperty().getTenantId()).build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(requestInfo).isconnectionCalculation(false).build();
		try {
			Object response = serviceRequestRepository.fetchResult(sewerageServiceUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject sewerageobject = mapper.convertValue(sewerageConnection, JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			sewerageobject.put(totalAmount, calResponse.getCalculation().get(0).getTotalAmount());
			sewerageobject.put(applicationFee, calResponse.getCalculation().get(0).getFee());
			sewerageobject.put(serviceFee, calResponse.getCalculation().get(0).getCharge());
			sewerageobject.put(tax, calResponse.getCalculation().get(0).getTaxAmount());
			sewerageobject.put(pdfTaxhead, calResponse.getCalculation().get(0).getTaxHeadEstimates());
			BigDecimal slaDays = workflowService.getSlaForState(requestInfo.getUserInfo().getTenantId(), requestInfo,
					sewerageConnection.getApplicationStatus().name());
			sewerageobject.put(sla, slaDays.divide(BigDecimal.valueOf(SWConstants.DAYS_CONST)));
			sewerageobject.put(slaDate, slaDays.add(new BigDecimal(sewerageConnection.getConnectionExecutionDate())));
			String tenantId = sewerageConnection.getProperty().getTenantId().split("\\.")[0];
			return getFielStoreIdFromPDFService(sewerageobject, requestInfo, tenantId, applicationKey);
		} catch (Exception ex) {
			log.error("Calculation response error!!", ex);
			throw new CustomException("SEWERAGE_CALCULATION_EXCEPTION", "Calculation response can not parsed!!!");
		}
	}

	/**
	 * Get file store id from PDF service
	 * 
	 * @param sewerageobject
	 * @param requestInfo
	 * @param tenantId
	 * @param applicationKey
	 * @return
	 */
	private String getFielStoreIdFromPDFService(JSONObject sewerageobject, RequestInfo requestInfo, String tenantId,
			String applicationKey) {
		JSONArray sewerageconnectionlist = new JSONArray();
		sewerageconnectionlist.add(sewerageobject);
		JSONObject requestPayload = new JSONObject();
		requestPayload.put(requestInfoReplacer, requestInfo);
		requestPayload.put(WaterConnectionReplacer, sewerageconnectionlist);
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
			throw new CustomException("SEWERAGE_FILESTORE_PDF_EXCEPTION", "PDF response can not parsed!!!");
		}
	}

	@SuppressWarnings("unchecked")
	public void process(SewerageConnectionRequest sewerageConnectionRequest, String topic) {
		HashMap<String, Object> addDetail = mapper
				.convertValue(sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails(), HashMap.class);
		if (addDetail.getOrDefault(SWConstants.ESTIMATION_FILESTORE_ID, null) == null) {
			addDetail.put(SWConstants.ESTIMATION_FILESTORE_ID,
					getFileStroeId(sewerageConnectionRequest.getSewerageConnection(),
							sewerageConnectionRequest.getRequestInfo(), SWConstants.PDF_ESTIMATION_KEY));
		}
		if (addDetail.getOrDefault(SWConstants.SANCTION_LETTER_FILESTORE_ID, null) == null) {
			addDetail.put(SWConstants.SANCTION_LETTER_FILESTORE_ID,
					getFileStroeId(sewerageConnectionRequest.getSewerageConnection(),
							sewerageConnectionRequest.getRequestInfo(), SWConstants.PDF_SANCTION_KEY));
		}
		sewerageConnectionRequest.getSewerageConnection().setAdditionalDetails(addDetail);
		sewerageDao.saveFileStoreIds(sewerageConnectionRequest);
	}
}