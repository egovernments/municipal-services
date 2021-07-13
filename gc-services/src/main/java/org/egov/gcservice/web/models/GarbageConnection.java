
package org.egov.gcservice.web.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.gcservice.web.models.workflow.ProcessInstance;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This is lightweight property object that can be used as reference by
 * definitions needing property linking. Actual Property Object extends this to
 * include more elaborate attributes of the property.
 */
@ApiModel(description = "This is lightweight property object that can be used as reference by definitions needing property linking. Actual Property Object extends this to include more elaborate attributes of the property.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-05-20T12:22:10.265+05:30[Asia/Kolkata]")
public class GarbageConnection {
	@SafeHtml
	@JsonProperty("id")
	private String id = null;

	@SafeHtml
	@JsonProperty("tenantid")
	private String tenantId = null;

	@SafeHtml
	@JsonProperty("property_id	")
	private String propertyId = null;

	@SafeHtml
	@JsonProperty("applicationno")
	private String applicationNo = null;

	@SafeHtml
	@JsonProperty("status")
	private String status = null;

	/**
	 * Gets or Sets status
	 */
	public enum StatusEnum {
		ACTIVE("Active"),

		INACTIVE("Inactive");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static StatusEnum fromValue(String text) {
			for (StatusEnum b : StatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	//@JsonProperty("status")
	//private StatusEnum status = null;

	@SafeHtml
	@JsonProperty("connectionno")
	private String connectionNo = null;

	@SafeHtml
	@JsonProperty("oldconnectionno")
	private String oldConnectionNo = null;

//	@JsonProperty("documents")
//	@Valid
//	private List<Document> documents = null;
//
//	@JsonProperty("plumberInfo")
//	@Valid
	//	private List<PlumberInfo> plumberInfo = null;
	@SafeHtml
	@JsonProperty("action")
	private String action = null;

	@SafeHtml
	@JsonProperty("plotsize")
	private String plotSize = null;

	@JsonProperty("street")
	private Float street = null;

//	@JsonProperty("roadCuttingInfo")
//	private List<RoadCuttingInfo> roadCuttingInfo = null;

	@JsonProperty("pincode")
	private Long pinCode = null;

	@SafeHtml
	@JsonProperty("buildingname")
	private String buildingName = null;

	@SafeHtml
	@JsonProperty("doorno")
	private String doorNo = null;

	@SafeHtml
	@JsonProperty("propertyType")
	private String propertyType = null;

	@JsonProperty("usagetype")
	private String usageType = null;

	@JsonProperty("applicationtype")
	private String applicationType = null;
	
	@JsonProperty("processInstance")
	private ProcessInstance processInstance = null;

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;
	
	@JsonProperty("connectionHolders")
	@Valid
	private List<OwnerInfo> connectionHolders;

	@SafeHtml
	@JsonProperty("occupancy")
	private String occupancy = null;

	@SafeHtml
	@JsonProperty("connectioncategory")
	private String connectionCategory = null;

	@JsonProperty("adhocrebate")
	private String  adhocRebate = null;

	@JsonProperty("adhocpenalty")
	private String  adhocPenalty = null;

	@JsonProperty("adhocpenaltyreason")
	private String  adhocPenaltyReason = null;

	@JsonProperty("adhocpenaltycomment")
	private String  adhocPenaltyComment = null;

	@JsonProperty("adhocrebatereason")
	private String  adhocRebateReason = null;

	@JsonProperty("adhocrebatecomment")
	private String  adhocRebateComment = null;

	@JsonProperty("islegacy")
	private Boolean islegacy = false;

	@JsonProperty("familymembers")
	private Long familyMembers = null;

	@JsonProperty("effectivefrom")
	private Long effectiveFrom = null;

	@JsonProperty("additionalDetails")
	private JsonNode additionalDetails=null;
	
	public GarbageConnection id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Unique Identifier of the connection for internal reference.
	 * 
	 * @return id
	 **/
	@ApiModelProperty(readOnly = true, value = "Unique Identifier of the connection for internal reference.")

	@Size(min = 1, max = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GarbageConnection tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Unique ULB identifier.
	 * 
	 * @return tenantId
	 **/
	@ApiModelProperty(value = "Unique ULB identifier.")

	@Size(min = 2, max = 256)
	@NotNull
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	
	/**
	 * Formatted connection number, which will be generated using ID-Gen service
	 * after aproval of connection application in case of new application. If the
	 * source of data is \"DATA_ENTRY\" then application status will be considered
	 * as \"APROVED\" application.
	 * 
	 * @return connectionNo
	 **/
	@ApiModelProperty(readOnly = true, value = "Formatted connection number, which will be generated using ID-Gen service after aproval of connection application in case of new application. If the source of data is \"DATA_ENTRY\" then application status will be considered as \"APROVED\" application.")

	@Size(min = 1, max = 64)
	public String getConnectionNo() {
		return connectionNo;
	}

	public void setConnectionNo(String connectionNo) {
		this.connectionNo = connectionNo;
	}

	public GarbageConnection oldConnectionNo(String oldConnectionNo) {
		this.oldConnectionNo = oldConnectionNo;
		return this;
	}

	/**
	 * Mandatory if source is \"DATA_ENTRY\".
	 * 
	 * @return oldConnectionNo
	 **/
	@ApiModelProperty(readOnly = true, value = "Mandatory if source is \"DATA_ENTRY\".")

	@Size(min = 1, max = 64)
	public String getOldConnectionNo() {
		return oldConnectionNo;
	}

	public void setOldConnectionNo(String oldConnectionNo) {
		this.oldConnectionNo = oldConnectionNo;
	}

		/**
	 * It is a master data, defined in MDMS
	 * 
	 * @return connectionCategory
	 **/
	@ApiModelProperty(required = true, value = "It is a master data, defined in MDMS")
	@Size(min = 2, max = 32)
	public String getConnectionCategory() {
		return connectionCategory;
	}

	public void setConnectionCategory(String connectionCategory) {
		this.connectionCategory = connectionCategory;
	}

	public GarbageConnection additionalDetails(JsonNode additionalDetails) {
		this.additionalDetails = additionalDetails;
		return this;
	}

	/**
	 * Json object to capture any extra information which is not accommodated of
	 * model
	 * 
	 * @return additionalDetails
	 **/
	@ApiModelProperty(value = "Json object to capture any extra information which is not accommodated of model")

	public JsonNode getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(JsonNode additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	
	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public String getPropertyId() {
		return propertyId;
	}

	public GarbageConnection propertyId(String propertyId) {
		this.propertyId = propertyId;
		return this;
	}
	
	public GarbageConnection addConnectionHolderInfo(OwnerInfo connectionHolderInfo) {
		if (this.connectionHolders == null) {
			this.connectionHolders = new ArrayList<OwnerInfo>();
		}
		if (!this.connectionHolders.contains(connectionHolderInfo))
			this.connectionHolders.add(connectionHolderInfo);
		return this;
	}

	@ApiModelProperty(value = "The connection holder info will enter by employee or citizen")
	@Valid
	public List<OwnerInfo> getConnectionHolders() {
		return connectionHolders;
	}

	public void setConnectionHolders(List<OwnerInfo> connectionHolders) {
		this.connectionHolders = connectionHolders;
	}
	
	public GarbageConnection dateEffectiveFrom(Long effectiveFrom) {
		this.effectiveFrom =effectiveFrom;
		return this;
	}

	/**
	 * Get dateEffectiveFrom
	 *
	 * @return dateEffectiveFrom
	 **/
	@ApiModelProperty(readOnly = true, value = "")
	@Valid
	public Long getEffectiveFrom() {
		return effectiveFrom;
	}

	public void setEffectiveFrom(Long dateEffectiveFrom) {
		this.effectiveFrom = dateEffectiveFrom;
	}

	
	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GarbageConnection garbageConnection = (GarbageConnection) o;
		return Objects.equals(this.id, garbageConnection.id) && Objects.equals(this.tenantId, garbageConnection.tenantId)
				&& Objects.equals(this.propertyId, garbageConnection.propertyId)
				&& Objects.equals(this.applicationNo, garbageConnection.applicationNo)
				&& Objects.equals(this.status, garbageConnection.status)
				&& Objects.equals(this.connectionNo, garbageConnection.connectionNo)
				&& Objects.equals(this.oldConnectionNo, garbageConnection.oldConnectionNo)			
				&& Objects.equals(this.connectionCategory, garbageConnection.connectionCategory)
				&& Objects.equals(this.propertyType, garbageConnection.propertyType)
				&& Objects.equals(this.additionalDetails, garbageConnection.additionalDetails)
				&& Objects.equals(this.connectionHolders, garbageConnection.connectionHolders)
				&& Objects.equals(this.usageType, garbageConnection.usageType)
				&& Objects.equals(this.effectiveFrom, garbageConnection.effectiveFrom)
				&& Objects.equals(this.islegacy	, garbageConnection.islegacy);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tenantId, propertyId, applicationNo, status, status, connectionNo,
				oldConnectionNo, connectionCategory, applicationType,usageType,propertyType,additionalDetails, auditDetails, connectionHolders,
				applicationType, effectiveFrom, islegacy);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class GarbageConnection {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    propertyId: ").append(toIndentedString(propertyId)).append("\n");
		sb.append("    applicationNo: ").append(toIndentedString(applicationNo)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    connectionNo: ").append(toIndentedString(connectionNo)).append("\n");
		sb.append("    oldConnectionNo: ").append(toIndentedString(oldConnectionNo)).append("\n");
		sb.append("    connectionCategory: ").append(toIndentedString(connectionCategory)).append("\n");
		sb.append("    applicationType: ").append(toIndentedString(applicationType)).append("\n");
		sb.append("    propertyType: ").append(toIndentedString(propertyType)).append("\n");
		sb.append("    usageType: ").append(toIndentedString(usageType)).append("\n");
		sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
		sb.append("    connectionHolders: ").append(toIndentedString(connectionHolders)).append("\n");
		sb.append("    familyMembers: ").append(toIndentedString(familyMembers)).append("\n");
		sb.append("	   effectiveFrom: ").append(toIndentedString(effectiveFrom)).append("\n");
		sb.append("	   isleagcy: ").append(toIndentedString(islegacy)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getPlotSize() {
		return plotSize;
	}

	public void setPlotSize(String plotSize) {
		this.plotSize = plotSize;
	}

	public Float getStreet() {
		return street;
	}

	public void setStreet(Float street) {
		this.street = street;
	}

	public Long getPinCode() {
		return pinCode;
	}

	public void setPinCode(Long pinCode) {
		this.pinCode = pinCode;
	}

	public String getBuildingName() {
		return buildingName;
	}

	public void setBuildingName(String buildingName) {
		this.buildingName = buildingName;
	}

	public String getDoorNo() {
		return doorNo;
	}

	public void setDoorNo(String doorNo) {
		this.doorNo = doorNo;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(String propertyType) {
		this.propertyType = propertyType;
	}

	public String getUsageType() {
		return usageType;
	}

	public void setUsageType(String usageType) {
		this.usageType = usageType;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	public String getOccupancy() {
		return occupancy;
	}

	public void setOccupancy(String occupancy) {
		this.occupancy = occupancy;
	}

	public String getAdhocRebate() {
		return adhocRebate;
	}

	public void setAdhocRebate(String adhocRebate) {
		this.adhocRebate = adhocRebate;
	}

	public String getAdhocPenalty() {
		return adhocPenalty;
	}

	public void setAdhocPenalty(String adhocPenalty) {
		this.adhocPenalty = adhocPenalty;
	}

	public String getAdhocPenaltyReason() {
		return adhocPenaltyReason;
	}

	public void setAdhocPenaltyReason(String adhocPenaltyReason) {
		this.adhocPenaltyReason = adhocPenaltyReason;
	}

	public String getAdhocPenaltyComment() {
		return adhocPenaltyComment;
	}

	public void setAdhocPenaltyComment(String adhocPenaltyComment) {
		this.adhocPenaltyComment = adhocPenaltyComment;
	}

	public String getAdhocRebateReason() {
		return adhocRebateReason;
	}

	public void setAdhocRebateReason(String adhocRebateReason) {
		this.adhocRebateReason = adhocRebateReason;
	}

	public String getAdhocRebateComment() {
		return adhocRebateComment;
	}

	public void setAdhocRebateComment(String adhocRebateComment) {
		this.adhocRebateComment = adhocRebateComment;
	}

	public Boolean getIslegacy() {
		return islegacy;
	}

	public void setIslegacy(Boolean islegacy) {
		this.islegacy = islegacy;
	}

	public Long getFamilyMembers() {
		return familyMembers;
	}

	public void setFamilyMembers(Long familyMembers) {
		this.familyMembers = familyMembers;
	}

	public void setAdditionalDetails(HashMap<String, Object> addtionalDetails) {
		// TODO Auto-generated method stub
		
	}
	
	
}