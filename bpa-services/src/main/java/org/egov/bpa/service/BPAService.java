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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.egov.bpa.repository.BPARepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAUtil;
import org.egov.bpa.util.NotificationUtil;
import org.egov.bpa.validator.BPAValidator;
import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPA.RiskTypeEnum;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.BPASearchCriteria;
import org.egov.bpa.web.models.OwnerInfo;
import org.egov.bpa.web.models.user.UserDetailResponse;
import org.egov.bpa.web.models.workflow.BusinessService;
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
	private UserService userService;

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

	public BPA create(BPARequest bpaRequest) {

		Object mdmsData = util.mDMSCall(bpaRequest);
		if (bpaRequest.getBPA().getTenantId().split("\\.").length == 1) {
			throw new CustomException(" Invalid Tenant ", " Application cannot be create at StateLevel");
		}
		edcrService.validateEdcrPlan(bpaRequest, mdmsData);
		bpaValidator.validateCreate(bpaRequest, mdmsData);
		enrichmentService.enrichBPACreateRequest(bpaRequest, mdmsData);

		userService.createUser(bpaRequest);

		wfIntegrator.callWorkFlow(bpaRequest);

		if (bpaRequest.getBPA().getRiskType().equals(RiskTypeEnum.LOW)) {
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
	public List<BPA> search(BPASearchCriteria criteria, RequestInfo requestInfo) {
		List<BPA> bpa;
		bpaValidator.validateSearch(requestInfo, criteria);
		if (criteria.getMobileNumber() != null) {
			bpa = getBPAFromMobileNumber(criteria, requestInfo);
		} else {
			List<String> roles = new ArrayList<>();
			for (Role role : requestInfo.getUserInfo().getRoles()) {
				roles.add(role.getCode());
			}

			if ((criteria.tenantIdOnly() || criteria.isEmpty()) && roles.contains(BPAConstants.CITIZEN)) {
				criteria.setCreatedBy(requestInfo.getUserInfo().getUuid());
			}

			bpa = getBPAWithOwnerInfo(criteria, requestInfo);
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
	public List<BPA> getBPAWithOwnerInfo(BPASearchCriteria criteria, RequestInfo requestInfo) {
		List<BPA> bpa = repository.getBPAData(criteria);
		if (bpa.isEmpty())
			return Collections.emptyList();
		bpa = enrichmentService.enrichBPASearch(bpa, criteria, requestInfo);
		return bpa;
	}

	private List<BPA> getBPAFromMobileNumber(BPASearchCriteria criteria, RequestInfo requestInfo) {

		List<BPA> bpa = new LinkedList<>();
		UserDetailResponse userDetailResponse = userService.getUser(criteria, requestInfo);
		// If user not found with given user fields return empty list
		if (userDetailResponse.getUser().size() == 0) {
			return Collections.emptyList();
		}
		enrichmentService.enrichBPACriteriaWithOwnerids(criteria, userDetailResponse);
		bpa = repository.getBPAData(criteria);

		if (bpa.size() == 0) {
			return Collections.emptyList();
		}

		// Add bpaId of all bpa's owned by the user
		criteria = enrichmentService.getBPACriteriaFromIds(bpa, criteria.getLimit());
		// Get all bpa with ownerInfo enriched from user service
		bpa = getBPAWithOwnerInfo(criteria, requestInfo);
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
		Object mdmsData = util.mDMSCall(bpaRequest);
		BPA bpa = bpaRequest.getBPA();

		if (bpa.getId() == null) {
			throw new CustomException("UPDATE ERROR", "Application Not found in the System" + bpa);
		}

		bpa.getOwners().forEach(owner -> {
			if (owner.getOwnerType() == null) {
				owner.setOwnerType("NONE");
			}
		});
		BusinessService businessService = workflowService.getBusinessService(bpa, bpaRequest.getRequestInfo(),
				bpa.getApplicationNo());

		List<BPA> searchResult = getBPAWithOwnerInfo(bpaRequest);
		if (CollectionUtils.isEmpty(searchResult)) {
			throw new CustomException("UPDATE ERROR", "Failed to Update the Application");
		}

		bpaRequest.getBPA().setAuditDetails(searchResult.get(0).getAuditDetails());
		enrichmentService.enrichBPAUpdateRequest(bpaRequest, businessService);

		if (bpa.getAction() != null && (bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_REJECT)
				|| bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_REVOCATE))) {

			if (bpa.getComment() == null || bpa.getComment().isEmpty()) {
				throw new CustomException("BPA_UPDATE_ERROR_COMMENT_REQUIRED",
						"Comment is mandaotory, please provide the comments ");
			}

		} else {
			userService.createUser(bpaRequest);
			if (!bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_SENDBACKTOCITIZEN)) {
			        actionValidator.validateUpdateRequest(bpaRequest, businessService);
				bpaValidator.validateUpdate(bpaRequest, searchResult, mdmsData,
					workflowService.getCurrentState(bpa.getStatus(), businessService));
				bpaValidator.validateCheckList(mdmsData, bpaRequest,
						workflowService.getCurrentState(bpa.getStatus(), businessService));
			}
		}

		wfIntegrator.callWorkFlow(bpaRequest);

		enrichmentService.postStatusEnrichment(bpaRequest);

		log.info("Bpa status is : " + bpa.getStatus());

		if (bpa.getAction().equalsIgnoreCase(BPAConstants.ACTION_APPLY)) {

			// generate sanction fee demand as well for the low risk application
			if (bpaRequest.getBPA().getRiskType().equals(RiskTypeEnum.LOW)) {
				calculationService.addCalculation(bpaRequest, BPAConstants.LOW_RISK_PERMIT_FEE_KEY);
			} else {
				calculationService.addCalculation(bpaRequest, BPAConstants.APPLICATION_FEE_KEY);
			}
		}

		// Generate the sanction Demand
		if (bpa.getStatus().equalsIgnoreCase(BPAConstants.SANC_FEE_STATE)) {
			calculationService.addCalculation(bpaRequest, BPAConstants.SANCTION_FEE_KEY);
		}
		repository.update(bpaRequest, workflowService.isStateUpdatable(bpa.getStatus(), businessService));

		List<OwnerInfo> activeOwners = bpaRequest.getBPA().getOwners().stream().filter(o -> o.getActive())
				.collect(Collectors.toList());
		bpaRequest.getBPA().getOwners().clear();
		bpaRequest.getBPA().setOwners(activeOwners);

		return bpaRequest.getBPA();

	}

	/**
	 * Returns bpa from db for the update request
	 * 
	 * @param request
	 *            The update request
	 * @return List of bpas
	 */
	public List<BPA> getBPAWithOwnerInfo(BPARequest request) {
		BPASearchCriteria criteria = new BPASearchCriteria();
		List<String> ids = new LinkedList<>();
		ids.add(request.getBPA().getId());

		criteria.setTenantId(request.getBPA().getTenantId());
		criteria.setIds(ids);

		List<BPA> bpa = repository.getBPAData(criteria);

		bpa = enrichmentService.enrichBPASearch(bpa, criteria, request.getRequestInfo());
		return bpa;
	}

	@SuppressWarnings("resource")
	public void getEdcrPdf(BPARequest bpaRequest) {

		byte[] ba1 = new byte[1024];
		int baLength;
		String fileName = BPAConstants.EDCR_PDF;
		PDDocument doc = null;
		BPA bpa = bpaRequest.getBPA();

		if (StringUtils.isEmpty(bpa.getPermitOrderNo())) {
			throw new CustomException("INVALID_REQUEST", "Permit Order No is required.");
		}

		try {
			String pdfUrl = edcrService.getEDCRPdfUrl(bpaRequest);
			URL url1 = new URL(pdfUrl);
			FileOutputStream fos1 = new FileOutputStream(fileName);
			System.out.print("Connecting to " + url1.toString() + " ... ");
			URLConnection urlConn = url1.openConnection();

			// Checking whether the URL contains a PDF
			if (!urlConn.getContentType().equalsIgnoreCase("application/pdf")) {
				throw new CustomException("INVALID_CONTENT", "Unable to get pdf from the EDCR");
			} else {

				// Read the PDF from the URL and save to a local file
				InputStream is1 = url1.openStream();
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
				String permitNo = notificationUtil.getMessageTemplate(BPAConstants.PERMIT_ORDER_NO,
						localizationMessages);
				permitNo = permitNo != null ? permitNo : BPAConstants.PERMIT_ORDER_NO;
				String generatedOn = notificationUtil.getMessageTemplate(BPAConstants.GENERATEDON,
						localizationMessages);
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

					contentStream.showText(permitNo + " : " + bpaRequest.getBPA().getPermitOrderNo());
					if (bpa.getOrderGeneratedDate() != null) {
						Date date = new Date(bpa.getOrderGeneratedDate());
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
			}
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
