package org.egov.pt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.request.Role;
import org.egov.pt.models.enums.Relationship;
import org.egov.pt.models.enums.Status;
import org.egov.pt.models.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class OwnerInfo extends User {

	@JsonProperty("ownerInfoUuid")
	private String ownerInfoUuid;
	
	@JsonProperty("gender")
	private String gender;

	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName;

	@JsonProperty("correspondenceAddress")
	private String correspondenceAddress;

	@JsonProperty("isPrimaryOwner")
	private Boolean isPrimaryOwner;

	@JsonProperty("ownerShipPercentage")
	private Double ownerShipPercentage;

	@NotNull
	@JsonProperty("ownerType")
	private String ownerType;

	@JsonProperty("institutionId")
	private String institutionId;
	
	@JsonProperty("status")
	private Status status;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents;

	@JsonProperty("relationship")
	private Relationship relationship;

	public OwnerInfo addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<>();
		}
		this.documents.add(documentsItem);
		return this;
	}

	/**
	 * Populates Owner fields from the given User object
	 * 
	 * @param user User object obtained from user service
	 */
	public void addUserDetail(User user) {
		this.setLastModifiedDate(user.getLastModifiedDate());
		this.setLastModifiedBy(user.getLastModifiedBy());
		this.setCreatedBy(user.getCreatedBy());
		this.setCreatedDate(user.getCreatedDate());
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

	@Builder()
	public OwnerInfo(Long id, String uuid, String userName, String password, String salutation, String name,
			String gender, String mobileNumber, String emailId, String altContactNumber, String pan,
			String aadhaarNumber, String permanentAddress, String permanentCity, String permanentPincode,
			String correspondenceCity, String correspondencePincode, String correspondenceAddress, Boolean active,
			Long dob, Long pwdExpiryDate, String locale, String type, String signature, Boolean accountLocked,
			List<Role> roles, String fatherOrHusbandName, String bloodGroup, String identificationMark, String photo,
			String createdBy, Long createdDate, String lastModifiedBy, Long lastModifiedDate, String tenantId,
			String ownerInfoUuid, String mobileNumber2, String gender2, String fatherOrHusbandName2,
			String correspondenceAddress2, Boolean isPrimaryOwner, Double ownerShipPercentage, String ownerType,
			String institutionId, Status status, List<Document> documents, Relationship relationship) {
		super(id, uuid, userName, password, salutation, name, gender, mobileNumber, emailId, altContactNumber, pan,
				aadhaarNumber, permanentAddress, permanentCity, permanentPincode, correspondenceCity,
				correspondencePincode, correspondenceAddress, active, dob, pwdExpiryDate, locale, type, signature,
				accountLocked, roles, fatherOrHusbandName, bloodGroup, identificationMark, photo, createdBy,
				createdDate, lastModifiedBy, lastModifiedDate, tenantId);
		this.ownerInfoUuid = ownerInfoUuid;
		mobileNumber = mobileNumber2;
		gender = gender2;
		fatherOrHusbandName = fatherOrHusbandName2;
		correspondenceAddress = correspondenceAddress2;
		this.isPrimaryOwner = isPrimaryOwner;
		this.ownerShipPercentage = ownerShipPercentage;
		this.ownerType = ownerType;
		this.institutionId = institutionId;
		this.status = status;
		this.documents = documents;
		this.relationship = relationship;
	}
	
	
	public OwnerInfo(OwnerInfo ownerInfo) {

		super(ownerInfo.getId(), ownerInfo.getUuid(), ownerInfo.getUserName(), ownerInfo.getPassword(), ownerInfo.getSalutation(),
				ownerInfo.getName(), ownerInfo.getGender(), ownerInfo.getMobileNumber(), ownerInfo.getEmailId(),
				ownerInfo.getAltContactNumber(), ownerInfo.getPan(), ownerInfo.getAadhaarNumber(), ownerInfo.getPermanentAddress(),
				ownerInfo.getPermanentCity(), ownerInfo.getPermanentPincode(), ownerInfo.getCorrespondenceCity(),
				ownerInfo.getCorrespondencePincode(), ownerInfo.getCorrespondenceAddress(), ownerInfo.getActive(), ownerInfo.getDob(),
				ownerInfo.getPwdExpiryDate(), ownerInfo.getLocale(), ownerInfo.getType(), ownerInfo.getSignature(),
				ownerInfo.getAccountLocked(), ownerInfo.getRoles(), ownerInfo.getFatherOrHusbandName(), ownerInfo.getBloodGroup(),
				ownerInfo.getIdentificationMark(), ownerInfo.getPhoto(), ownerInfo.getCreatedBy(), ownerInfo.getCreatedDate(),
				ownerInfo.getLastModifiedBy(), ownerInfo.getLastModifiedDate(), ownerInfo.getTenantId());

		this.ownerInfoUuid = ownerInfo.getOwnerInfoUuid();
		this.fatherOrHusbandName = ownerInfo.getFatherOrHusbandName();
		this.correspondenceAddress = ownerInfo.getCorrespondenceAddress();
		this.isPrimaryOwner = ownerInfo.getIsPrimaryOwner();
		this.ownerShipPercentage = ownerInfo.getOwnerShipPercentage();
		this.ownerType = ownerInfo.getOwnerType();
		this.institutionId = ownerInfo.getInstitutionId();
		this.status = ownerInfo.getStatus();
		this.documents = copyDocuments(ownerInfo.getDocuments());
		this.relationship = ownerInfo.getRelationship();
	}

	private @Valid List<Document> copyDocuments(List<Document> documents) {
		
		List<Document> newDocs = new ArrayList<>();
		documents.forEach(doc -> {
			newDocs.add(doc.toBuilder().build());
		});
		return newDocs;
	}

}