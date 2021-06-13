package org.egov.fsm.web.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataMartModel {

	private String applicationId = "N/A";

	private String applicationStatus = "N/A";

	private String reasonForRejection = "N/A";

	private String reasonForReassignment = "N/A";

	private String reasonForCancellation = "N/A";

	private String propertyType;

	private String propertySubType;

	private String sanitationType;

	private String doorNo = "N/A";

	private String streetName = "N/A";

	private String city = "N/A";

	private String locality = "N/A";

	private String pinCode = "N/A";

	private String district = "N/A";

	private String state = "N/A";

	private String slumName = "N/A";
	
	private boolean isGeoLocationProvided=false;

	private String longtitude = "N/A";

	private String latitude = "N/A";

	private String applicationSource = "N/A";

	private LocalDateTime createdAssignedDateTime;
	
	private String createdStatus;
	
	private LocalDateTime pendingForPaymentAssignedTime;
	
	private String pendingForPaymentStatus;
	
	private String assignDsoStatus;
	
	private LocalDateTime assignDsoAssignedDateTime;
	
	private String dsoInprogressStatus;
	
	private LocalDateTime dsoInprogressAssignedTime;
	
	private String dsoPendingApprovalStatus;
	
	private LocalDateTime dsoPendingAssignedTime;
	
	private String complatedStatus;
	
	private LocalDateTime applicationCompletedTime;

	private long slaDays = 0;

	private long slaPlanned = 0;

	private String desludgingEntity;

	private String desludgingVechicleNumber;

	private String reasonForDecline;

	private String vechileType;

	private LocalDateTime vechileInDateTime;

	private LocalDateTime vechileOutDateTime;

	private int vechicleCapacity = 0;

	private int wasteCollected = 0;

	private int wasteDumped = 0;

	private double paymentAmount = 0;

	private String paymentStatus = "N/A";

	private String paymentSource = "N/A";

	private String paymentInstrumentType = "N/A";

	private Integer rating = 0;

	private String fstpPlantName = "N/A";

	private String fstpPlantOperator = "N/A";
	
	private String dsoRejectedStatus="N/A";
	
	private LocalDateTime dsoRejectedDateTime;
	
	private String rejectedStatus="N/A";
	
	private LocalDateTime rejectedDateTime;
	
	private String cancelledStatus="N/A";
	
	private LocalDateTime cancelledDateTime;
	
	private String citizanFeedbackStatus;
	
	private LocalDateTime citizanFeedbackDateTime;
	

}
