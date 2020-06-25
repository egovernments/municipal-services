package org.egov.swservice.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.egov.common.contract.request.RequestInfo;
import org.egov.swservice.config.SWConfiguration;
import org.egov.swservice.model.Calculation;
import org.egov.swservice.model.CalculationCriteria;
import org.egov.swservice.model.CalculationReq;
import org.egov.swservice.model.CalculationRes;
import org.egov.swservice.model.Property;
import org.egov.swservice.model.SewerageConnectionRequest;
import org.egov.swservice.repository.ServiceRequestRepository;
import org.egov.swservice.repository.SewarageDaoImpl;
import org.egov.swservice.util.SWConstants;
import org.egov.swservice.util.SewerageServicesUtil;
import org.egov.swservice.validator.ValidateProperty;
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
	 * @param sewerageConnection
	 * @param requestInfo
	 * @param applicationKey
	 * @return file store id
	 */
	public String getFileStroeId(SewerageConnectionRequest sewerageConnectionRequest, Property property,
			String applicationKey) {
		CalculationCriteria criteria = CalculationCriteria.builder()
				.applicationNo(sewerageConnectionRequest.getSewerageConnection().getApplicationNo())
				.sewerageConnection(sewerageConnectionRequest.getSewerageConnection()).tenantId(property.getTenantId())
				.build();
		CalculationReq calRequest = CalculationReq.builder().calculationCriteria(Arrays.asList(criteria))
				.requestInfo(sewerageConnectionRequest.getRequestInfo()).isconnectionCalculation(false).build();
		
		String applicationStatus = workflowService.getApplicationStatus(sewerageConnectionRequest.getRequestInfo(),
				sewerageConnectionRequest.getSewerageConnection().getApplicationNo(),
				sewerageConnectionRequest.getSewerageConnection().getTenantId());
		
		try {
			Object response = serviceRequestRepository.fetchResult(sewerageServiceUtil.getEstimationURL(), calRequest);
			CalculationRes calResponse = mapper.convertValue(response, CalculationRes.class);
			JSONObject sewerageobject = mapper.convertValue(sewerageConnectionRequest.getSewerageConnection(),
					JSONObject.class);
			if (CollectionUtils.isEmpty(calResponse.getCalculation())) {
				throw new CustomException("NO_ESTIMATION_FOUND", "Estimation not found!!!");
			}
			Optional<Calculation> calculationList = calResponse.getCalculation().stream().findFirst();
			if(calculationList.isPresent()) {
				Calculation cal = calculationList.get();
				sewerageobject.put(totalAmount, cal.getTotalAmount());
				sewerageobject.put(applicationFee, cal.getFee());
				sewerageobject.put(serviceFee, cal.getCharge());
				sewerageobject.put(tax, cal.getTaxAmount());
				cal.getTaxHeadEstimates().forEach(item -> {
					//We need to remove SW_ --> So that PDF configuration refers the common for both Water & Sewerage
					item.setTaxHeadCode(item.getTaxHeadCode().substring(3));
				});
				sewerageobject.put(pdfTaxhead, cal.getTaxHeadEstimates());
			}
			sewerageobject.put(sanctionLetterDate, System.currentTimeMillis());
			BigDecimal slaDays = workflowService.getSlaForState(
					sewerageConnectionRequest.getSewerageConnection().getTenantId(),
					sewerageConnectionRequest.getRequestInfo(), applicationStatus);
			sewerageobject.put(sla, slaDays.divide(BigDecimal.valueOf(SWConstants.DAYS_CONST)));
			sewerageobject.put(slaDate, slaDays.add(
					new BigDecimal(System.currentTimeMillis())));
			String[] tenantDetails = property.getTenantId().split("\\."); 
			String tenantId = tenantDetails[0];
			if(tenantDetails.length > 1)
			{
				sewerageobject.put(tenantName, tenantDetails[1].toUpperCase());
			}
			sewerageobject.put(propertyKey, property);
			sewerageobject.put(service, "SEWERAGE");
			return getFielStoreIdFromPDFService(sewerageobject, sewerageConnectionRequest.getRequestInfo(), tenantId,
					applicationKey);
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

		Property property = validateProperty.getOrValidateProperty(sewerageConnectionRequest);

		HashMap<String, Object> addDetail = mapper
				.convertValue(sewerageConnectionRequest.getSewerageConnection().getAdditionalDetails(), HashMap.class);
		if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(SWConstants.APPROVE_CONNECTION_CONST)
				&& addDetail.getOrDefault(SWConstants.ESTIMATION_FILESTORE_ID, null) == null) {
			addDetail.put(SWConstants.ESTIMATION_DATE_CONST, System.currentTimeMillis());
			addDetail.put(SWConstants.ESTIMATION_FILESTORE_ID,
					getFileStroeId(sewerageConnectionRequest, property, SWConstants.PDF_ESTIMATION_KEY));
		}
		if (sewerageConnectionRequest.getSewerageConnection().getProcessInstance().getAction()
				.equalsIgnoreCase(SWConstants.ACTION_PAY)
				&& addDetail.getOrDefault(SWConstants.SANCTION_LETTER_FILESTORE_ID, null) == null) {
			addDetail.put(SWConstants.SANCTION_LETTER_FILESTORE_ID,
					getFileStroeId(sewerageConnectionRequest, property, SWConstants.PDF_SANCTION_KEY));
		}
		sewerageConnectionRequest.getSewerageConnection().setAdditionalDetails(addDetail);
		sewerageDao.saveFileStoreIds(sewerageConnectionRequest);
	}
}
