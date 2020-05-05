package org.egov.bpa.web.model;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerInfo extends UserInfo {
	 @JsonProperty("name")
	  private String name;

	  @JsonProperty("mobileNumber")
	  private String mobileNumber;

	  @JsonProperty("gender")
	  private String gender;

	  @JsonProperty("fatherOrHusbandName")
	  private String fatherOrHusbandName;

	  @JsonProperty("correspondenceAddress")
	  private String correspondenceAddress;

	  @JsonProperty("isPrimaryOwner")
	  private Boolean isPrimaryOwner;

	  @JsonProperty("ownerShipPercentage")
	  private BigDecimal ownerShipPercentage;

	  @JsonProperty("ownerType")
	  private String ownerType;

	  @JsonProperty("institutionId")
	  private String institutionId;

	  @JsonProperty("documents")
	  private List<Document> documents;

	  @JsonProperty("relationship")
	  private Relationship relationship;

	  @JsonProperty("additionalDetails")
	  private Object additionalDetails;

	  
	  /**
	     * Populates Owner fields from the given User object
	     * @param user User object obtained from user service
	     */
	    public void addUserWithoutAuditDetail(OwnerInfo user){
	            this.setUuid(user.getUuid());
//	            this.setId(user.getId());
	            this.setUserName(user.getUserName());
	            this.setPassword(user.getPassword());
//	            this.setSalutation(user.getSalutation());
	            this.setName(user.getName());
	            this.setGender(user.getGender());
	            this.setMobileNumber(user.getMobileNumber());
//	            this.setEmailId(user.getEmailId());
//	            this.setAltContactNumber(user.getAltContactNumber());
//	            this.setPan(user.getPan());
//	            this.setAadhaarNumber(user.getAadhaarNumber());
//	            this.setPermanentAddress(user.getPermanentAddress());
//	            this.setPermanentCity(user.getPermanentCity());
//	            this.setPermanentPincode(user.getPermanentPincode());
	            this.setCorrespondenceAddress(user.getCorrespondenceAddress());
//	            this.setCorrespondenceCity(user.getCorrespondenceCity());
//	            this.setCorrespondencePincode(user.getCorrespondencePincode());
//	            this.setActive(user.getActive());
//	            this.setDob(user.getDob());
//	            this.setPwdExpiryDate(user.getPwdExpiryDate());
//	            this.setLocale(user.getLocale());
//	            this.setType(user.getType());
//	            this.setAccountLocked(user.getAccountLocked());
//	            this.setRoles(user.getRoles());
	            this.setFatherOrHusbandName(user.getFatherOrHusbandName());
//	            this.setBloodGroup(user.getBloodGroup());
//	            this.setIdentificationMark(user.getIdentificationMark());
//	            this.setPhoto(user.getPhoto());
	            this.setTenantId(user.getTenantId());
	    }

	    /**
	     * Populates Owner fields from the given User object
	     * @param user User object obtained from user service
	     */
	    public void addUserDetail(UserInfo user){
//	            this.setId(user.getId());
//	            this.setLastModifiedDate(user.getLastModifiedDate());
//	            this.setLastModifiedBy(user.getLastModifiedBy());
//	            this.setCreatedBy(user.getCreatedBy());
//	            this.setCreatedDate(user.getCreatedDate());
	            this.setUserName(user.getUserName());
	            this.setPassword(user.getPassword());
//	            this.setSalutation(user.getSalutation());
//	            this.setName(user.getName());
//	            this.setGender(user.getGender());
//	            this.setMobileNumber(user.getMobileNumber());
//	            this.setEmailId(user.getEmailId());
//	            this.setAltContactNumber(user.getAltContactNumber());
//	            this.setPan(user.getPan());
//	            this.setAadhaarNumber(user.getAadhaarNumber());
//	            this.setPermanentAddress(user.getPermanentAddress());
//	            this.setPermanentCity(user.getPermanentCity());
//	            this.setPermanentPincode(user.getPermanentPincode());
//	            this.setCorrespondenceAddress(user.getCorrespondenceAddress());
//	            this.setCorrespondenceCity(user.getCorrespondenceCity());
//	            this.setCorrespondencePincode(user.getCorrespondencePincode());
//	            this.setActive(user.getActive());
//	            this.setDob(user.getDob());
//	            this.setPwdExpiryDate(user.getPwdExpiryDate());
//	            this.setLocale(user.getLocale());
//	            this.setType(user.getType());
//	            this.setAccountLocked(user.getAccountLocked());
//	            this.setRoles(user.getRoles());
//	            this.setFatherOrHusbandName(user.getFatherOrHusbandName());
//	            this.setBloodGroup(user.getBloodGroup());
//	            this.setIdentificationMark(user.getIdentificationMark());
//	            this.setPhoto(user.getPhoto());
	            this.setTenantId(user.getTenantId());
	    }

	    public OwnerInfo(org.egov.common.contract.request.User user){
	            this.setTenantId(user.getTenantId());
	            this.setUserName(user.getUserName());
//	            this.setId(user.getId());
	            this.setName(user.getName());
//	            this.setType(user.getType());
	            this.setMobileNumber(user.getMobileNumber());
//	            this.setEmailId(user.getEmailId());
//	            this.setRoles(addRoles(user.getRoles()));
	            this.setUuid(user.getUuid());
	    }

	    public void addCitizenDetail(UserInfo user){
	            this.setTenantId(user.getTenantId());
	            this.setUserName(user.getUserName());
	            /*this.setId(user.getId());
	            this.setName(user.getName());
	            this.setType(user.getType());
	            this.setMobileNumber(user.getMobileNumber());
	            this.setEmailId(user.getEmailId());
	            this.setRoles(user.getRoles());*/
	            this.setUuid(user.getUuid());
	    }

}
