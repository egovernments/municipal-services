package org.egov.bpa.util;

import org.springframework.stereotype.Component;

@Component
public class BPAConstants {

	// MDMS

	public static final String BPA_MODULE = "BPA";

	public static final String BPA_MODULE_CODE = "BPA";

	public static final String COMMON_MASTERS_MODULE = "common-masters";

	public static final String NOTIFICATION_LOCALE = "en_IN";

	public static final String NOTIFICATION_INITIATED = "bpa.en.counter.initiate";

	public static final String NOTIFICATION_APPLIED = "bpa.en.counter.submit";

	public static final String NOTIFICATION_DOCUMENT_VERIFICATION = "bpa.en.document";

	public static final String NOTIFICATION_FIELD_INSPECTION = "bpa.en.field.inspection";

	public static final String NOTIFICATION_NOC_UPDATION = "bpa.en.field.inspection";

	public static final String NOTIFICATION_PAYMENT_OWNER = "bpa.en.counter.payment.successful.owner";

	public static final String NOTIFICATION_PAYMENT_PAYER = "bpa.en.counter.payment.successful.payer";

	public static final String NOTIFICATION_PAID = "bpa.en.counter.pending.approval";

	public static final String NOTIFICATION_APPROVED = "bpa.en.counter.approved";

	public static final String NOTIFICATION_REJECTED = "bpa.en.counter.rejected";

	public static final String NOTIFICATION_CANCELLED = "bpa.en.counter.cancelled";

	public static final String NOTIFICATION_FIELD_CHANGED = "bpa.en.edit.field.change";

	public static final String NOTIFICATION_OBJECT_ADDED = "bpa.en.edit.object.added";

	public static final String NOTIFICATION_OBJECT_REMOVED = "bpa.en.edit.object.removed";

	public static final String NOTIFICATION_OBJECT_MODIFIED = "bpa.en.edit.object.modified";

	public static final String DEFAULT_OBJECT_MODIFIED_MSG = "Dear <1>,Your Building Plan with application number <APPLICATION_NUMBER> was modified.";

	// mdms path codes

	public static final String BPA_JSONPATH_CODE = "$.MdmsRes.BPA";

	public static final String COMMON_MASTER_JSONPATH_CODE = "$.MdmsRes.common-masters";

	// error constants

	public static final String INVALID_TENANT_ID_MDMS_KEY = "INVALID TENANTID";

	public static final String INVALID_TENANT_ID_MDMS_MSG = "No data found for this tenentID";

	// mdms master names

	public static final String SERVICE_TYPE = "ServiceType";
	
	public static final String APPLICATION_TYPE = "ApplicationType";
	
	public static final String OCCUPANCY_TYPE = "OccupancyType";
	
	public static final String SUB_OCCUPANCY_TYPE = "SubOccupancyType";
	
	public static final String USAGES = "Usages";
	
	public static final String DOCUMENT_TYPE_MAPPING = "DocTypeMapping";

	public static final String RISKTYPE_COMPUTATION = "RiskTypeComputation";
	
	public static final String DOCUMENT_TYPE = "DocumentType";
	
	public static final String OWNER_TYPE = "OwnerType";
	
	public static final String OWNERSHIP_CATEGORY = "OwnerShipCategory";
	

	// FINANCIAL YEAR

	public static final String MDMS_EGF_MASTER = "egf-master";

	public static final String MDMS_FINANCIALYEAR = "FinancialYear";

	public static final String MDMS_FINACIALYEAR_PATH = "$.MdmsRes.egf-master.FinancialYear[?(@.code==\"{}\")]";

	public static final String MDMS_STARTDATE = "startingDate";

	public static final String MDMS_ENDDATE = "endingDate";
	

	// BPA actions

	public static final String ACTION_INITIATE = "INITIATE";

	public static final String ACTION_APPLY = "APPLY";

	public static final String ACTION_APPROVE = "APPROVE";

	public static final String ACTION_FORWORD = "FORWARD";
	
	public static final String ACTION_MARK = "MARK";
	
	public static final String ACTION_SENDBACK="SENDBACK";

	public static final String ACTION_DOC_VERIFICATION_FORWARD = "DOC_VERIFICATION_FORWARD";

	public static final String ACTION_FIELDINSPECTION_FORWARD = "FIELDINSPECTION_FORWARD";

	public static final String ACTION_NOC_FORWARD = "NOC_FORWARD";

	public static final String ACTION_PENDINGAPPROVAL = "PENDINGAPPROVAL";

	public static final String ACTION_REJECT = "REJECT";

	public static final String ACTION_CANCEL = "CANCEL";

	public static final String ACTION_PAY = "PAY";

	public static final String ACTION_ADHOC = "ADHOC";
	

	// BPA Status

	public static final String STATUS_INITIATED = "INPROGRESS";

	public static final String STATUS_APPLIED = "INPROGRESS";

	public static final String STATUS_APPROVED = "APPROVED";

	public static final String STATUS_REJECTED = "REJECTED";

	public static final String STATUS_DOCUMENTVERIFICATION = "INPROGRESS";

	public static final String STATUS_FIELDINSPECTION = "INPROGRESS";

	public static final String STATUS_NOCUPDATION = "INPROGRESS";

	public static final String STATUS_PENDINGAPPROVAL = "INPROGRESS";

	public static final String STATUS_CANCELLED = "CANCELLED";

	public static final String STATUS_PAID = "INPROGRESS";

	public static final String BILL_AMOUNT_JSONPATH = "$.billResponse.Bill[0].billDetails[0].totalAmount";
	

	// ACTION_STATUS combinations for notification

	public static final String ACTION_STATUS_INITIATED = "INITIATED_ACTIVE";

	public static final String ACTION_STATUS_APPLIED = "APPLIED";

	public static final String ACTION_STATUS_APPROVED = "APPROVE_PENDINGPAYMENT";

	public static final String ACTION_STATUS_REJECTED = "REJECT_REJECTED";

	public static final String ACTION_STATUS_DOCUMENTVERIFICATION = "FORWARD_DOCUMENTVERIFICATION";

//	public static final String ACTION_CANCEL_CANCELLED = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_PAID = "PAID";

	public static final String ACTION_STATUS_FIELDINSPECTION = "FORWARD_FIELDINSPECTION";

    public static final String ACTION_CANCEL_CANCELLED  = "CANCEL_CANCELLED";

	public static final String ACTION_STATUS_NOCUPDATION = "FORWARD_NOCUPDATION";
    
    
    

	public static final String USREVENTS_EVENT_TYPE = "SYSTEMGENERATED";
	public static final String USREVENTS_EVENT_NAME = "Building Plan";
	public static final String USREVENTS_EVENT_POSTEDBY = "SYSTEM-BPA";
	
	
	// OCCUPANCY TYPE
	
	public static final String RESIDENTIAL_OCCUPANCY = "A";
	
	// CALCULATION FEEe
	public static final String APPLICATION_FEE_KEY="ApplicationFee";
	public static final String SANCTION_FEE_KEY="SanctionFee";
	
	public static final String SANC_FEE_STATE="PENDING_SANC_FEE_PAYMENT";
	public static final String APPROVED_STATE ="APPROVED";


}
