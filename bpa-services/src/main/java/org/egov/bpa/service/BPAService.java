package org.egov.bpa.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.model.BPA;
import org.egov.bpa.web.model.BPARequest;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.bpa.web.model.landInfo.LandInfo;
import org.egov.bpa.web.model.landInfo.LandSearchCriteria;
import org.egov.bpa.web.model.user.UserDetailResponse;
import org.egov.bpa.web.model.user.UserSearchRequest;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.workflow.ActionValidator;
import org.egov.bpa.workflow.WorkflowIntegrator;
import org.egov.bpa.workflow.WorkflowService;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.request.Role;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
	private UserService userService;
	
	@Autowired
	private NocService nocService;
	
	@Autowired
	private BPAConfiguration config;
	
	/**
	 * does all the validations required to create BPA Record in the system
	 * @param bpaRequest
	 * @return
	 */
	public BPA create(BPARequest bpaRequest) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		String tenantId = bpaRequest.getBPA().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		if (bpaRequest.getBPA().getTenantId().split("\\.").length == 1) {
			throw new CustomException(BPAErrorConstants.INVALID_TENANT, " Application cannot be create at StateLevel");
		}
		
		//Since approval number should be generated at approve stage
		if(!StringUtils.isEmpty(bpaRequest.getBPA().getApprovalNo())) {
			bpaRequest.getBPA().setApprovalNo(null);
		}
		
		Map<String, String> values = edcrService.validateEdcrPlan(bpaRequest, mdmsData);
		String applicationType = values.get(BPAConstants.APPLICATIONTYPE);
		this.validateCreateOC(applicationType, values, requestInfo, bpaRequest);
		bpaValidator.validateCreate(bpaRequest, mdmsData, values);
		if (!applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			landService.addLandInfoToBPA(bpaRequest);
		}
		enrichmentService.enrichBPACreateRequest(bpaRequest, mdmsData, values);
		wfIntegrator.callWorkFlow(bpaRequest);
		nocService.createNocRequest(bpaRequest, mdmsData);
		this.addCalculation(applicationType, bpaRequest);
		repository.save(bpaRequest);
		return bpaRequest.getBPA();
	}

	/**
	 * applies the required vlaidation for OC on Create
	 * @param applicationType
	 * @param values
	 * @param criteria
	 * @param requestInfo
	 * @param bpaRequest
	 */
	private void validateCreateOC(String applicationType,Map<String, String> values, RequestInfo requestInfo, BPARequest bpaRequest) {
		
		if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			String approvalNo = values.get(BPAConstants.PERMIT_NO);

			BPASearchCriteria criteria = new BPASearchCriteria();
			criteria.setTenantId(bpaRequest.getBPA().getTenantId());
			criteria.setApprovalNo(approvalNo);
			List<BPA> ocBpas = search(criteria, requestInfo);
			
			if( ocBpas.size() <=0 || ocBpas.size() >1) {
				throw new CustomException(BPAErrorConstants.CREATE_ERROR, 
						( (ocBpas.size() <=0) ? "BPA not found with approval Number :" : "Multiple BPA applications found for approval number :") + approvalNo);
			}
			else if ( ocBpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REVOCATED)) {
				throw new CustomException(BPAErrorConstants.CREATE_ERROR, "This permit number is revocated you cannot use this permit number");
			}
			else if (!ocBpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_APPROVED)) {
				throw new CustomException(BPAErrorConstants.CREATE_ERROR, "The selected permit number still in workflow approval process, Please apply occupancy after completing approval process.");
			}

			values.put("landId", ocBpas.get(0).getLandId());
			criteria.setEdcrNumber(ocBpas.get(0).getEdcrNumber());
			ocService.validateAdditionalData(bpaRequest, criteria);
			bpaRequest.getBPA().setLandInfo(ocBpas.get(0).getLandInfo());
		}
	}
	
	/**
	 * calls calculation service calculate and generte demand accordingly
	 * @param applicationType
	 * @param bpaRequest
	 */
	private void addCalculation(String applicationType,BPARequest bpaRequest) {
		
		if (bpaRequest.getBPA().getRiskType().equals(BPAConstants.LOW_RISKTYPE) && !applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			calculationService.addCalculation(bpaRequest, BPAConstants.LOW_RISK_PERMIT_FEE_KEY);
		} else {
			calculationService.addCalculation(bpaRequest, BPAConstants.APPLICATION_FEE_KEY);
		}
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
	public List<BPA> search(BPASearchCriteria criteria, RequestInfo requestInfo) {
		List<BPA> bpas = new LinkedList<>();
		bpaValidator.validateSearch(requestInfo, criteria);
		LandSearchCriteria landcriteria = new LandSearchCriteria();
		landcriteria.setTenantId(criteria.getTenantId());
		List<String> edcrNos = null;
		if (criteria.getMobileNumber() != null) {
			bpas= this.getBPAFromMobileNumber(criteria, landcriteria, requestInfo);
		} else {
			List<String> roles = new ArrayList<>();
			for (Role role : requestInfo.getUserInfo().getRoles()) {
				roles.add(role.getCode());
			}
			if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(BPAConstants.CITIZEN)) {
				log.debug("loading data of created and by me");
				bpas =  this.getBPACreatedForByMe(criteria, requestInfo, landcriteria, edcrNos);
				log.debug("no of bpas retuning by the search query" + bpas.size());
			} else {
				bpas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
				ArrayList<String> landIds = new ArrayList<String>();
				if (bpas.size() > 0) {	
					for (int i = 0; i < bpas.size(); i++) {
						landIds.add(bpas.get(i).getLandId());
					}
					landcriteria.setIds(landIds);
					landcriteria.setTenantId(bpas.get(0).getTenantId());
					log.debug("Call with tenantId to Land::" + landcriteria.getTenantId());
					ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);

					this.populateLandToBPA(bpas, landInfos,requestInfo);
				}
			}
		}
		return bpas;
	}
	/**
	 * search the BPA records created by and create for the logged in User
	 * @param criteria
	 * @param requestInfo
	 * @param landcriteria
	 * @param edcrNos
	 * @param bpas
	 */
	private List<BPA> getBPACreatedForByMe(BPASearchCriteria criteria, RequestInfo requestInfo,LandSearchCriteria landcriteria,List<String> edcrNos ) {
		List<BPA> bpas = null;
		UserSearchRequest userSearchRequest = new UserSearchRequest();
		if (criteria.getTenantId() != null) {
			userSearchRequest.setTenantId(criteria.getTenantId());
		}
		List<String> uuids = new ArrayList<String>();
		if (requestInfo.getUserInfo() != null && !StringUtils.isEmpty(requestInfo.getUserInfo().getUuid())) {
			uuids.add(requestInfo.getUserInfo().getUuid());
			criteria.setOwnerIds(uuids);
			criteria.setCreatedBy(uuids);
		}
		log.debug("loading data of created and by me"+ uuids.toString());
		UserDetailResponse userInfo = userService.getUser(criteria, requestInfo);
		if (userInfo != null) {
			landcriteria.setMobileNumber(userInfo.getUser().get(0).getMobileNumber());
		}
		log.debug("Call with multiple to Land::" + landcriteria.getTenantId() + landcriteria.getMobileNumber());
		ArrayList<LandInfo> landInfos = landService.searchLandInfoToBPA(requestInfo, landcriteria);
		ArrayList<String> landIds = new ArrayList<String>();
		if (landInfos.size() > 0) {
			landInfos.forEach(land -> {
				landIds.add(land.getId());
			});
			criteria.setLandId(landIds);
		}
		
		bpas = getBPAFromCriteria(criteria, requestInfo, edcrNos);
		log.debug("no of bpas queried" + bpas.size());
		this.populateLandToBPA(bpas, landInfos, requestInfo);
		return bpas;
	}

	/**
	 * populate appropriate landInfo to BPA
	 * @param bpas
	 * @param landInfos
	 */
	private void populateLandToBPA(List<BPA> bpas, List<LandInfo> landInfos, RequestInfo requestInfo) {
		for (int i = 0; i < bpas.size(); i++) {
			for (int j = 0; j < landInfos.size(); j++) {
				if (landInfos.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
					bpas.get(i).setLandInfo(landInfos.get(j));
				}
			}
			if(bpas.get(i).getLandId() != null && bpas.get(i).getLandInfo() == null) {
				LandSearchCriteria missingLandcriteria = new LandSearchCriteria();
				List<String> missingLandIds = new ArrayList<String>();
				missingLandIds.add(bpas.get(i).getLandId());
				missingLandcriteria.setTenantId(bpas.get(0).getTenantId());
				missingLandcriteria.setIds(missingLandIds);
				log.debug("Call with land ids to Land::" + missingLandcriteria.getTenantId() + missingLandcriteria.getIds());
				List<LandInfo> newLandInfo = landService.searchLandInfoToBPA(requestInfo, missingLandcriteria);
				for (int j = 0; j < newLandInfo.size(); j++) {
					if (newLandInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
						bpas.get(i).setLandInfo(newLandInfo.get(j));
					}
				}
			}
		}
	}
	/**
	 * search the land with mobile number and then BPA from the land
	 * @param criteria
	 * @param landcriteria
	 * @param requestInfo
	 * @return
	 */
	private List<BPA> getBPAFromMobileNumber(BPASearchCriteria criteria,LandSearchCriteria landcriteria, RequestInfo requestInfo){
		List<BPA> bpas =null;
		log.debug("Call with mobile number to Land::" + criteria.getMobileNumber());
		landcriteria.setMobileNumber(criteria.getMobileNumber());
		ArrayList<LandInfo> landInfo = landService.searchLandInfoToBPA(requestInfo, landcriteria);
		ArrayList<String> landId = new ArrayList<String>();
		if (landInfo.size() > 0) {
			landInfo.forEach(land -> {
				landId.add(land.getId());
			});
			criteria.setLandId(landId);
		}
		bpas = getBPAFromLandId(criteria, requestInfo, null);
		if (landInfo.size() > 0) {
			for (int i = 0; i < bpas.size(); i++) {
				for (int j = 0; j < landInfo.size(); j++) {
					if (landInfo.get(j).getId().equalsIgnoreCase(bpas.get(i).getLandId())) {
						bpas.get(i).setLandInfo(landInfo.get(j));
					}
				}
			}
		}
		return bpas;
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
	@SuppressWarnings("unchecked")
	public BPA update(BPARequest bpaRequest) {
		RequestInfo requestInfo = bpaRequest.getRequestInfo();
		String tenantId = bpaRequest.getBPA().getTenantId().split("\\.")[0];
		Object mdmsData = util.mDMSCall(requestInfo, tenantId);
		BPA bpa = bpaRequest.getBPA();

		if (bpa.getId() == null) {
			throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "Application Not found in the System" + bpa);
		}

		Map<String, String> edcrResponse = edcrService.getEDCRDetails(bpaRequest.getRequestInfo(), bpaRequest.getBPA());
		String applicationType = edcrResponse.get(BPAConstants.APPLICATIONTYPE);
		log.debug("applicationType is " + applicationType);
		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		List<BPA> searchResult = getBPAWithBPAId(bpaRequest);
		if (CollectionUtils.isEmpty(searchResult) || searchResult.size() > 1) {
			throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "Failed to Update the Application, Found None or multiple applications!");
		}
		
		
		Map<String, String> additionalDetails = bpa.getAdditionalDetails() != null ? (Map)bpa.getAdditionalDetails()
				: new HashMap<String, String>();
		
		if (bpa.getStatus().equalsIgnoreCase(BPAConstants.FI_STATUS)
				&& bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)) {
			if (additionalDetails.get(BPAConstants.FI_ADDITIONALDETAILS) != null)
				additionalDetails.remove(BPAConstants.FI_ADDITIONALDETAILS);
		}
		
		this.processOcUpdate(applicationType,  edcrResponse.get(BPAConstants.PERMIT_NO), bpaRequest, requestInfo, additionalDetails);

		bpaRequest.getBPA().setAuditDetails(searchResult.get(0).getAuditDetails());
		
		nocService.manageOfflineNocs(bpaRequest, mdmsData);
		bpaValidator.validatePreEnrichData(bpaRequest, mdmsData);
		enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);
		
		this.handleRejectSendBackActions(applicationType, bpaRequest, businessService, searchResult, mdmsData, edcrResponse);

		
		wfIntegrator.callWorkFlow(bpaRequest);
		log.debug("===> workflow done =>" +bpaRequest.getBPA().getStatus()  );
		enrichmentService.postStatusEnrichment(bpaRequest);
		
		log.debug("Bpa status is : " + bpa.getStatus());

		// Generate the sanction Demand
		if (bpa.getStatus().equalsIgnoreCase(BPAConstants.SANC_FEE_STATE)) {
			calculationService.addCalculation(bpaRequest, BPAConstants.SANCTION_FEE_KEY);
		}

		if (Arrays.asList(config.getSkipPaymentStatuses().split(",")).contains(bpa.getStatus())) {
			enrichmentService.skipPayment(bpaRequest);
			enrichmentService.postStatusEnrichment(bpaRequest);
		}
		
		repository.update(bpaRequest, workflowService.isStateUpdatable(bpa.getStatus(), businessService));
		return bpaRequest.getBPA();

	}
	
	/**
	 * handle the reject and Send Back action of the update
	 * @param applicationType
	 * @param bpaRequest
	 * @param businessService
	 * @param searchResult
	 * @param mdmsData
	 * @param edcrResponse
	 */
	private void handleRejectSendBackActions(String applicationType, BPARequest bpaRequest,BusinessService businessService,List<BPA> searchResult,Object mdmsData,Map<String, String> edcrResponse ) {
		BPA bpa = bpaRequest.getBPA();
		if (bpa.getWorkflow().getAction() != null && (bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
				|| bpa.getWorkflow().getAction().equalsIgnoreCase(BPAConstants.ACTION_REVOCATE))) {

			if (bpa.getWorkflow().getComments() == null || bpa.getWorkflow().getComments().isEmpty()) {
				throw new CustomException(BPAErrorConstants.BPA_UPDATE_ERROR_COMMENT_REQUIRED,
						"Comment is mandaotory, please provide the comments ");
			}
			nocService.handleBPARejectedStateForNoc(bpaRequest);

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
	}

	/**
	 * for OC application update logic is handled which specific to OC
	 * @param applicationType
	 * @param approvalNo
	 * @param bpaRequest
	 * @param requestInfo
	 * @param additionalDetails
	 */
	private void processOcUpdate(String applicationType, String approvalNo, BPARequest bpaRequest,RequestInfo requestInfo,Map<String, String> additionalDetails ) {
		if (applicationType.equalsIgnoreCase(BPAConstants.BUILDING_PLAN_OC)) {
			
			BPASearchCriteria criteria = new BPASearchCriteria();
			criteria.setTenantId(bpaRequest.getBPA().getTenantId());
			criteria.setApprovalNo(approvalNo);
			List<BPA> bpas = search(criteria, requestInfo);
			if( bpas.size() <=0 || bpas.size() >1) {
				throw new CustomException(BPAErrorConstants.UPDATE_ERROR, 
						( (bpas.size() <=0) ? "BPA not found with approval Number :" : "Multiple BPA applications found for approval number :") + approvalNo);
			}
			else if ( bpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_REVOCATED)) {
				throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "This permit number is revocated you cannot use this permit number");
			}
			else if (!bpas.get(0).getStatus().equalsIgnoreCase(BPAConstants.STATUS_APPROVED)) {
				throw new CustomException(BPAErrorConstants.UPDATE_ERROR, "The selected permit number still in workflow approval process, Please apply occupancy after completing approval process.");
			}
			
			additionalDetails.put("landId", bpas.get(0).getLandId());
			criteria.setEdcrNumber(bpas.get(0).getEdcrNumber());
			ocService.validateAdditionalData(bpaRequest, criteria);
			bpaRequest.getBPA().setLandInfo(bpas.get(0).getLandInfo());
		}
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

	/**
	 * downloads the EDCR Report from the edcr system and stamp the permit no and generated date on the download pdf and return
	 * @param bpaRequest
	 */
	@SuppressWarnings("resource")
	public void getEdcrPdf(BPARequest bpaRequest) {
		
		String fileName = BPAConstants.EDCR_PDF;
		PDDocument document = null;
		BPA bpa = bpaRequest.getBPA();

		if (StringUtils.isEmpty(bpa.getApprovalNo())) {
			throw new CustomException(BPAErrorConstants.INVALID_REQUEST, "Approval Number is required.");
		}

		try {
			this.createTempReport(bpaRequest, fileName, document);
			String localizationMessages = notificationUtil.getLocalizationMessages(bpa.getTenantId(),
					bpaRequest.getRequestInfo());
			String permitNo = notificationUtil.getMessageTemplate(BPAConstants.PERMIT_ORDER_NO, localizationMessages);
			permitNo = permitNo != null ? permitNo : BPAConstants.PERMIT_ORDER_NO;
			String generatedOn = notificationUtil.getMessageTemplate(BPAConstants.GENERATEDON, localizationMessages);
			generatedOn = generatedOn != null ? generatedOn : BPAConstants.GENERATEDON;
			this.addDataToPdf(document, bpaRequest, permitNo, generatedOn,fileName);

		} catch (Exception ex) {
			log.debug("Exception occured while downloading pdf", ex.getMessage());
			throw new CustomException(BPAErrorConstants.UNABLE_TO_DOWNLOAD, "Unable to download the file");
		} finally {
			try {
				if (document != null) {
					document.close();
				}
			} catch (Exception ex) {
				throw new CustomException(BPAErrorConstants.INVALID_FILE, "unable to close this file");
			}
		}
	}
	
	/**
	 * make edcr call and get the edcr report url to download the edcr report
	 * @param bpaRequest
	 * @return
	 * @throws Exception
	 */
	private URL getEdcrReportDownloaUrl(BPARequest bpaRequest) throws Exception {
		String pdfUrl = edcrService.getEDCRPdfUrl(bpaRequest);
		URL downloadUrl = new URL(pdfUrl);
		
		log.debug("Connecting to redirect url" + downloadUrl.toString() + " ... ");
		URLConnection urlConnection = downloadUrl.openConnection();

		// Checking whether the URL contains a PDF
		if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
			String downloadUrlString = urlConnection.getHeaderField("Location");
			if (!StringUtils.isEmpty(downloadUrlString)) {
				downloadUrl = new URL(downloadUrlString);
				log.debug("Connecting to download url" + downloadUrl.toString() + " ... ");
				urlConnection = downloadUrl.openConnection();
				if (!urlConnection.getContentType().equalsIgnoreCase("application/pdf")) {
					log.error("Download url content type is not application/pdf.");
					throw new CustomException(BPAErrorConstants.INVALID_EDCR_REPORT,"Download url content type is not application/pdf.");
				}
			} else {
				log.error("Unable to fetch the location header URL");
				throw new CustomException(BPAErrorConstants.INVALID_EDCR_REPORT,"Unable to fetch the location header URL");
			}
		}
		return downloadUrl;
	}
	/**
	 * download the edcr report and create in tempfile
	 * @param bpaRequest
	 * @param fileName
	 * @param document
	 * @throws Exception
	 */
	private void createTempReport(BPARequest bpaRequest,String fileName,PDDocument document) throws Exception {
		URL downloadUrl = this.getEdcrReportDownloaUrl(bpaRequest);
		// Read the PDF from the URL and save to a local file
		FileOutputStream writeStream = new FileOutputStream(fileName);
		byte[] byteChunck = new byte[1024];
		int baLength;
		InputStream readStream = downloadUrl.openStream();
		while ((baLength = readStream.read(byteChunck)) != -1) {
			writeStream.write(byteChunck, 0, baLength);
		}
		writeStream.flush();
		writeStream.close();
		readStream.close();

		document = PDDocument.load(new File(fileName));
	}
	
	private void addDataToPdf(PDDocument document,BPARequest bpaRequest, String permitNo, String generatedOn,String fileName) throws IOException {
		PDPageTree allPages = document.getDocumentCatalog().getPages();
		BPA bpa = bpaRequest.getBPA();
		for (int i = 0; i < allPages.getCount(); i++) {
			PDPage page = (PDPage) allPages.get(i);
			@SuppressWarnings("deprecation")
			PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true, true);
			PDFont font = PDType1Font.TIMES_ROMAN;
			float fontSize = 10.0f;
			contentStream.beginText();
			// set font and font size
			contentStream.setFont(font, fontSize);

			PDRectangle mediabox = page.getMediaBox();
			float margin = 32;
			float startX = mediabox.getLowerLeftX() + margin;
			float startY = mediabox.getUpperRightY() - (margin/2);
			contentStream.newLineAtOffset(startX, startY);

			contentStream.showText(permitNo + " : " + bpaRequest.getBPA().getApprovalNo());
			if (bpa.getApprovalDate() != null) {
				Date date = new Date(bpa.getApprovalDate());
				DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
				String formattedDate = format.format(date);
				contentStream.newLineAtOffset(436, 0);
				contentStream.showText(generatedOn + " : " + formattedDate);
			} else {
				contentStream.newLineAtOffset(436, 0);
				contentStream.showText(generatedOn + " : " + "NA");
			}

			contentStream.endText();
			contentStream.close();
		}
		document.save(fileName);
	}
}
