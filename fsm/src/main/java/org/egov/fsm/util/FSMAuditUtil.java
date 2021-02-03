package org.egov.fsm.util;

import org.egov.fsm.web.model.AuditDetails;
import org.egov.fsm.web.model.FSM;
import org.egov.fsm.web.model.PitDetail;
import org.egov.fsm.web.model.FSM.StatusEnum;
import org.egov.fsm.web.model.location.Address;
import org.egov.fsm.web.model.location.Boundary;
import org.egov.fsm.web.model.user.User;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FSMAuditUtil {

	private String id = null;
	
	private String applicationNo = null;

	private String accountId = null;

	private String description = null;

	private String applicationStatus = null;

	private String source = null;

	private String sanitationtype = null;

	private String propertyUsage = null;

	private Integer noOfTrips = null;

	private String status = null;

	private String vehicleId = null;

	private String doorNo = null;

	private String plotNo = null;

	private String landmark = null;

	private String city = null;

	private String district = null;

	private String region = null;

	private String state = null;

	private String country = null;

	private String locality = null;

	private String pincode = null;

	private String buildingName = null;

	private String street = null;

	private Double latitude = null;

	private Double longitude = null;

	private Double height = null;

	private Double length = null;

	private Double width = null;

	private Double diameter = null;

	private Double distanceFromRoad = null;

	private String modifiedBy = null;

	private Long modifiedTime = null;

	private String createdBy = null;

	private Long createdTime = null;

}
