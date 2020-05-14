package org.egov.land.web.models;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerInfo extends User {

	@JsonProperty("ownerId")
	private String ownerId;
	
	@JsonProperty("isPrimaryOwner")
	private Boolean isPrimaryOwner;

	@JsonProperty("ownerShipPercentage")
	private Double ownerShipPercentage;

	@JsonProperty("ownerType")
	private String ownerType;

	@JsonProperty("institutionId")
	private String institutionId;

	// @JsonProperty("documents")
	// private List<Document> documents;

	@JsonProperty("relationship")
	private Relationship relationship;

	@JsonProperty("additionalDetails")
	private Object additionalDetails;

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails;

	/**
	 * Populates Owner fields from the given User object
	 * 
	 * @param user
	 *            User object obtained from user service
	 */
	public void addUserWithoutAuditDetail(OwnerInfo user) {
		this.setUuid(user.getUuid());
        this.setId(user.getId());
        this.setUserName(user.getUserName());
        this.setPassword(user.getPassword());
        this.setSalutation(user.getSalutation());
        this.setName(user.getName());
        this.setGender(user.getGender());
        this.setMobileNumber(user.getMobileNumber());
        this.setEmailId(user.getEmailId());
        this.setAltContactNumber(user.getAltContactNumber());
        this.setPan(user.getPan());
        this.setAadhaarNumber(user.getAadhaarNumber());
        this.setPermanentAddress(user.getPermanentAddress());
        this.setPermanentCity(user.getPermanentCity());
        this.setPermanentPincode(user.getPermanentPincode());
        this.setCorrespondenceAddress(user.getCorrespondenceAddress());
        this.setCorrespondenceCity(user.getCorrespondenceCity());
        this.setCorrespondencePincode(user.getCorrespondencePincode());
        this.setActive(user.getActive());
        this.setDob(user.getDob());
        this.setPwdExpiryDate(user.getPwdExpiryDate());
        this.setLocale(user.getLocale());
        this.setType(user.getType());
        this.setAccountLocked(user.getAccountLocked());
        this.setRoles(user.getRoles());
        this.setFatherOrHusbandName(user.getFatherOrHusbandName());
        this.setBloodGroup(user.getBloodGroup());
        this.setIdentificationMark(user.getIdentificationMark());
        this.setPhoto(user.getPhoto());
        this.setTenantId(user.getTenantId());
	}

	/**
	 * Populates Owner fields from the given User object
	 * 
	 * @param user
	 *            User object obtained from user service
	 */
	public void addUserDetail(User user) {
		this.setUserName(user.getUserName());
		this.setPassword(user.getPassword());
		this.setTenantId(user.getTenantId());
	}

	public OwnerInfo(org.egov.common.contract.request.User user) {
		this.setTenantId(user.getTenantId());
		this.setUserName(user.getUserName());
		this.setName(user.getName());
		this.setMobileNumber(user.getMobileNumber());
		this.setUuid(user.getUuid());
	}

	public void addCitizenDetail(User user) {
		this.setTenantId(user.getTenantId());
		this.setUserName(user.getUserName());
		this.setUuid(user.getUuid());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		return true;
	}

	@Builder
	public OwnerInfo(Long id, String uuid, String userName, String password, String salutation, String name,
			String gender, String mobileNumber, String emailId, String altContactNumber, String pan,
			String aadhaarNumber, String permanentAddress, String permanentCity, String permanentPincode,
			String correspondenceCity, String correspondencePincode, String correspondenceAddress, Boolean active,
			Long dob, Long pwdExpiryDate, String locale, String type, String signature, Boolean accountLocked,
			List<Role> roles, String fatherOrHusbandName, String bloodGroup, String identificationMark, String photo,
			String createdBy, Long createdDate, String lastModifiedBy, Long lastModifiedDate, String otpReference,
			String tenantId, String ownerId, Boolean isPrimaryOwner, Double ownerShipPercentage, String ownerType,
			String institutionId, Relationship relationship, Object additionalDetails, AuditDetails auditDetails) {
		super(id, uuid, userName, password, salutation, name, gender, mobileNumber, emailId, altContactNumber, pan,
				aadhaarNumber, permanentAddress, permanentCity, permanentPincode, correspondenceCity,
				correspondencePincode, correspondenceAddress, active, dob, pwdExpiryDate, locale, type, signature,
				accountLocked, roles, fatherOrHusbandName, bloodGroup, identificationMark, photo, createdBy,
				createdDate, lastModifiedBy, lastModifiedDate, otpReference, tenantId);
		this.ownerId = ownerId;
		this.isPrimaryOwner = isPrimaryOwner;
		this.ownerShipPercentage = ownerShipPercentage;
		this.ownerType = ownerType;
		this.institutionId = institutionId;
		this.relationship = relationship;
		this.additionalDetails = additionalDetails;
		this.auditDetails = auditDetails;
	}
}
