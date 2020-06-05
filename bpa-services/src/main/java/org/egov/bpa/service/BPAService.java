package org.egov.bpa.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.egov.bpa.web.model.user.UserSearchRequest;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.land.web.models.LandInfo;
import org.egov.land.web.models.LandSearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import io.micrometer.core.instrument.MeterRegistry.Config;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

@Service
@Slf4j
public class BPAService {

	@Autowired
	private WorkflowIntegrator wfIntegrator;

	@Autowired
	private EnrichmentService enrichmentService;

	@Autowired
	private EDCRService edcrService;

	@Autowired
	private BPARepository repository;

	@Autowired
	private ActionValidator actionValidator;

	@Autowired
	private BPAValidator bpaValidator;

	@Autowired
	private BPAUtil util;

	@Autowired
	private CalculationService calculationService;

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private BPALandService landService;

	@Autowired
	private OCService ocService;

	@Autowired
	private BPAConfiguration config;

	@Autowired
	private UserService userService;
	
	public BPA create(BPARequest bpaRequest) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		String tenantId = bpaRequest.getBPA().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (bpaRequest.getBPA().getTenantId().split("\\.").length == 1) {
			throw new CustomException(" Invalid Tenant ", " Application cannot be create at StateLevel");
		}
		
		//Since approval number should be generated at approve stage
		if(!StringUtils.isEmpty(bpaRequest.getBPA().getApprovalNo()))
			bpaRequest.getBPA().setApprovalNo(null);

		Map<String, String> values = edcrService.validateEdcrPlan(bpaRequest, mdmsData);
		String applicationType = values.get(BPAConstants.APPLICATIONTYPE);
		BPASearchCriteria criteria = new BPASearchCriteria();
		criteria.setTenantId(bpaRequest.getBPA().getTenantId());
		if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			String approvalNo = values.get(BPAConstants.PERMIT_NO);

			criteria.setApprovalNo(approvalNo);
			List<BPA> BPA = search(criteria, requestInfo);
			String edcr = null;
			String landId = null;

			for (int i = 0; i < BPA.size(); i++) {
				edcr = BPA.get(0).getEdcrNumber();
				landId = BPA.get(0).getLandId();
			}
			
			values.put("landId", landId);
			criteria.setEdcrNumber(edcr);
			ocService.validateAdditionalData(bpaRequest, criteria);
			bpaRequest.getBPA().setLandInfo(BPA.get(0).getLandInfo());
		}
		bpaValidator.validateCreate(bpaRequest, mdmsData, values);
		if (!applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			landService.addLandInfoToBPA(bpaRequest);
		}
		enrichmentService.enrichBPACreateRequest(bpaRequest, mdmsData, values);
		wfIntegrator.callWorkFlow(bpaRequest);

		if (bpaRequest.getBPA().getRiskType().equals(BPAConstants.LOW_RISKTYPE)) {
			calculationService.addCalculation(bpaRequest, BPAConstants.LOW_RISK_PERMIT_FEE_KEY);
		} else {
			calculationService.addCalculation(bpaRequest, BPAConstants.APPLICATION_FEE_KEY);
		}

		repository.save(bpaRequest);
		return bpaRequest.getBPA();
	}


	/**
	 * Searches the Bpa for the given criteria if search is on owner paramter
	 * then first user service is called followed by query to db
	 * 
	 * @param criteria
	 *            The object containing the parameters on which to search
	 * @param requestInfo
	 *            The search request's requestInfo
	 * @return List of bpa for the given criteria
	 */
	@SuppressWarnings("null")
	public List<BPA> search(BPASearchCriteria criteria, RequestInfo requestInfo) {
		List<BPA> bpa = new LinkedList<>();
		bpaValidator.validateSearch(requestInfo, criteria);
		LandSearchCriteria landcriteria = new LandSearchCriteria();
		landcriteria.setTenantId(criteria.getTenantId());
		List<String> edcrNos = null;
		if (criteria.getApplicationType() != null || criteria.getServiceType() != null) {
			ArrayList<String> business = util.getBusinessService(criteria.getApplicationType(), criteria.getServiceType());
			if(business.size()>0){
			criteria.setBusinessService(business);
			}
		}
		if (criteria.getMobileNumber() != null) {
			landcriteria.setMobileNumber(criteria.getMobileNumber());
			ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
			ArrayList<String> landId = new ArrayList<String>();
			if (landInfo.size() > 0) {
				landInfo.forEach(land -> {
					landId.add(land.getId());
				});
				criteria.setLandId(landId);
			}
			bpa = getBPAFromLandId(criteria, requestInfo, edcrNos);
			if (landInfo.size() > 0) {
				for (int i = 0; i < bpa.size(); i++) {
					for (int j = 0; j < landInfo.size(); j++) {
						if (landInfo.get(j).getId().equalsIgnoreCase(bpa.get(i).getLandId())) {
							bpa.get(i).setLandInfo(landInfo.get(j));
						}
					}
				}
			}
		} else {
			if (criteria.getRequestor()!=null) {
				if (criteria.getTenantId() != null) {
					landcriteria.setTenantId(criteria.getTenantId());
				}
				UserSearchRequest userSearchRequest = new UserSearchRequest();
				userSearchRequest.setUuid(criteria.getRequestor());
				
				UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
//				userService.userCall(userSearchRequest, uri);
				landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
				ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
				ArrayList<String> landId = new ArrayList<String>();
				if (landInfo.size() > 0) {
					landInfo.forEach(land -> {
						landId.add(land.getId());
					});
					criteria.setLandId(landId);
				}
				bpa = getBPAFromCriteria(criteria, requestInfo, edcrNos);
				for (int i = 0; i < bpa.size(); i++) {
					for (int j = 0; j < landInfo.size(); j++) {
						if (landInfo.get(j).getId().equalsIgnoreCase(bpa.get(i).getLandId())) {
							bpa.get(i).setLandInfo(landInfo.get(j));
						}
					}
				}
			} else {
				bpa = getBPAFromCriteria(criteria, requestInfo, edcrNos);
				ArrayList<String> data = new ArrayList<String>();
				if (bpa.size() > 0) {
					for (int i = 0; i < bpa.size(); i++) {
						data.add(bpa.get(i).getLandId());
					}
					landcriteria.setIds(data);
					landcriteria.setTenantId(bpa.get(0).getTenantId());
					ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);

					for (int i = 0; i < bpa.size(); i++) {
						for (int j = 0; j < landInfo.size(); j++) {
							if (landInfo.get(j).getId().equalsIgnoreCase(bpa.get(i).getLandId())) {
								bpa.get(i).setLandInfo(landInfo.get(j));
							}
						}
					}
				}
			}
		}
		return bpa;
	}

	


	private List<BPA> getBPAFromLandId(BPASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
		List<BPA> bpa = new LinkedList<>();
		bpa = repository.getBPAData(criteria, edcrNos);
		if (bpa.size() == 0) {
			return Collections.emptyList();
		}
		return bpa;
	}


	/**
	 * Returns the bpa with enriched owners from user service
	 * 
	 * @param criteria
	 *            The object containing the parameters on which to search
	 * @param requestInfo
	 *            The search request's requestInfo
	 * @return List of bpa for the given criteria
	 */
	public List<BPA> getBPAFromCriteria(BPASearchCriteria criteria, RequestInfo requestInfo, List<String> edcrNos) {
		List<BPA> bpa = repository.getBPAData(criteria, edcrNos);
		if (bpa.isEmpty())
			return Collections.emptyList();
		return bpa;
	}

	/**
	 * Updates the bpa
	 * 
	 * @param bpaRequest
	 *            The update Request
	 * @return Updated bpa
	 */
	public BPA update(BPARequest bpaRequest) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		String tenantId = bpaRequest.getBPA().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		BPA bpa = bpaRequest.getBPA();

		if (bpa.getId() == null) {
			throw new CustomException("UPDATE ERROR", "Application Not found in the System" + bpa);
		}

		Map<String, String> edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(), bpaRequest.getBPA());
		String applicationType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
		log.info("applicationType is " + applicationType);
		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		List<BPA> searchResult = getBPAWithBPAId(bpaRequest);
		if (CollectionUtils.isEmpty(searchResult)) {
			throw new CustomException("UPDATE ERROR", "Failed to Update the Application");
		}
		
		BPASearchCriteria criteria = new BPASearchCriteria();
		criteria.setTenantId(bpaRequest.getBPA().getTenantId());
		Map<String, String> additionalDetails = bpa.getAdditionalDetails() != null ? (Map)bpa.getAdditionalDetails()
				: new HashMap<String, String>();
		if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			String approvalNo = edcrResponse.get(BPAConstants.PERMIT_NO);

			criteria.setApprovalNo(approvalNo);
			List<BPA> BPA = search(criteria, requestInfo);
			String edcr = null;
			String landId = null;

			for (int i = 0; i < BPA.size(); i++) {
				edcr = BPA.get(0).getEdcrNumber();
				landId = BPA.get(0).getLandId();
			}
			
			additionalDetails.put("landId", landId);
			criteria.setEdcrNumber(edcr);
			ocService.validateAdditionalData(bpaRequest, criteria);
			bpaRequest.getBPA().setLandInfo(BPA.get(0).getLandInfo());
		}

		bpaRequest.getBPA().setAuditDetails(searchResult.get(0).getAuditDetails());
		enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);

		if (bpa.getWorkflow().getAction() != null && (bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
				|| bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REVOCATE))) {

			if (bpa.getWorkflow().getComments() == null || bpa.getWorkflow().getComments().isEmpty()) {
				throw new CustomException("BPA_UPDATE_ERROR_COMMENT_REQUIRED",
						"Comment is mandaotory, please provide the comments ");
			}

		} else {
			if (!bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)) {
				actionValidator.validateUpdateRequest(bpaRequest, businessService);
				bpaValidator.validateUpdate(bpaRequest, searchResult, mdmsData,
						workflowService.getCurrentState(bpa.getStatus(), businessService), edcrResponse);
				if (!applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
					landService.updateLandInfo(bpaRequest);
				}
				bpaValidator.validateCheckList(mdmsData, bpaRequest,
						workflowService.getCurrentState(bpa.getStatus(), businessService));
			}
		}

		wfIntegrator.callWorkFlow(bpaRequest);

		enrichmentService.postStatusEnrichment(bpaRequest);

		log.info("Bpa status is : " + bpa.getStatus());

		// Generate the sanction Demand
		if (bpa.getStatus().equalsIgnoreCase(BPAConstants.SANC_FEE_STATE)) {
			calculationService.addCalculation(bpaRequest, BPAConstants.SANCTION_FEE_KEY);
		}
		
		repository.update(bpaRequest, workflowService.isStateUpdatable(bpa.getStatus(), businessService));
		return bpaRequest.getBPA();

	}


	/**
	 * Returns bpa from db for the update request
	 * 
	 * @param request
	 *            The update request
	 * @return List of bpas
	 */
	public List<BPA> getBPAWithBPAId(BPARequest request) {
		BPASearchCriteria criteria = new BPASearchCriteria();
		List<String> ids = new LinkedList<>();
		ids.add(request.getBPA().getId());
		criteria.setTenantId(request.getBPA().getTenantId());
		criteria.setIds(ids);
		List<BPA> bpa = repository.getBPAData(criteria, null);
		return bpa;
	}

	@SuppressWarnings("resource")
	public void getEdcrPdf(BPARequest bpaRequest) {

		byte[] ba1 = new byte[1024];
		int baLength;
		String fileName = BPAConstants.EDCR_PDF;
		PDDocument doc = null;
		BPA bpa = bpaRequest.getBPA();

		if (StringUtils.isEmpty(bpa.getApprovalNo())) {
			throw new CustomException("INVALID_REQUEST", "Permit Order No is required.");
		}

		try {
			String pdfUrl = edcrService.getEDCRPdfUrl(bpaRequest);
			URL downloadUrl = new URL(pdfUrl);
			FileOutputStream fos1 = new FileOutputStream(fileName);
			log.info("Connecting to redirect url" + downloadUrl.toString() + " ... ");
			URLConnection urlConnection = downloadUrl.openConnection();

			// Checking whether the URL contains a PDF
			if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
				String downloadUrlString = urlConnection.getHeaderField("Location");
				if (!StringUtils.isEmpty(downloadUrlString)) {
					downloadUrl = new URL(downloadUrlString);
					log.info("Connecting to download url" + downloadUrl.toString() + " ... ");
					urlConnection = downloadUrl.openConnection();
					if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
						log.error("Download url content type is not application/pdf.");
						throw new Exception();
					}
				} else {
					log.error("Unable to fetch the location header URL");
					throw new Exception();
				}
			}
			// Read the PDF from the URL and save to a local file
			InputStream is1 = downloadUrl.openStream();
			while ((baLength = is1.read(ba1)) != -1) {
				fos1.write(ba1, 0, baLength);
			}
			fos1.flush();
			fos1.close();
			is1.close();

			doc = PDDocument.load(new File(fileName));

			PDPageTree allPages = doc.getDocumentCatalog().getPages();

			String localizationMessages = notificationUtil.getLocalizationMessages(bpa.getTenantId(),
					bpaRequest.getRequestInfo());
			String permitNo = notificationUtil.getMessageTemplate(BPAConstants.PERMIT_ORDER_NO, localizationMessages);
			permitNo = permitNo != null ? permitNo : BPAConstants.PERMIT_ORDER_NO;
			String generatedOn = notificationUtil.getMessageTemplate(BPAConstants.GENERATEDON, localizationMessages);
			generatedOn = generatedOn != null ? generatedOn : BPAConstants.GENERATEDON;

			for (int i = 0; i < allPages.getCount(); i++) {
				PDPage page = (PDPage) allPages.get(i);
				@SuppressWarnings("deprecation")
				PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, true, true);
				PDFont font = PDType1Font.TIMES_ROMAN;
				float fontSize = 12.0f;
				contentStream.beginText();
				// set font and font size
				contentStream.setFont(font, fontSize);

				PDRectangle mediabox = page.getMediaBox();
				float margin = 20;
				float startX = mediabox.getLowerLeftX() + margin;
				float startY = mediabox.getUpperRightY() - margin;
				contentStream.newLineAtOffset(startX, startY);

				contentStream.showText(permitNo + " : " + bpaRequest.getBPA().getApprovalNo());
				if (bpa.getApprovalDate() != null) {
					Date date = new Date(bpa.getApprovalDate());
					DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
					String formattedDate = format.format(date);
					contentStream.newLineAtOffset(400, 4.5f);
					contentStream.showText(generatedOn + " : " + formattedDate);
				} else {
					contentStream.newLineAtOffset(400, 4.5f);
					contentStream.showText(generatedOn + " : " + "NA");
				}

				contentStream.endText();
				contentStream.close();
			}
			doc.save(fileName);

		} catch (Exception ex) {
			log.info("Exception occured while downloading pdf", ex.getMessage());
			throw new CustomException("UNABLE_TO_DOWNLOAD", "Unable to download the file");
		} finally {
			try {
				if (doc != null) {
					doc.close();
				}
			} catch (Exception ex) {
				throw new CustomException("INVALID_FILE", "UNABLE CLOSE THE FILE");
			}
		}
	}
}
