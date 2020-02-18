package org.egov.waterConnection.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * This is lightweight property object that can be used as reference by
 * definitions needing property linking. Actual Property Object extends this to
 * include more elaborate attributes of the property.
 */
@ApiModel(description = "This is lightweight property object that can be used as reference by definitions needing property linking. Actual Property Object extends this to include more elaborate attributes of the property.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-01-22T12:39:45.543+05:30[Asia/Kolkata]")
public class Connection {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("property")
	private Property property = null;

	@JsonProperty("applicationNo")
	private String applicationNo = null;

	/**
	 * Gets or Sets applicationStatus
	 */
	public enum ApplicationStatusEnum {
		INITIATED("INITIATED"),

		REJECTED("REJECTED"),

		PENDING_FOR_CITIZEN_ACTION("PENDING_FOR_CITIZEN_ACTION"),

		PENDING_FOR_DOCUMENT_VERIFICATION("PENDING_FOR_DOCUMENT_VERIFICATION"),

		PENDING_FOR_FIELD_INSPECTION("PENDING_FOR_FIELD_INSPECTION"),

		PENDING_APPROVAL_FOR_CONNECTION("PENDING_APPROVAL_FOR_CONNECTION"),

		PENDING_FOR_PAYMENT("PENDING_FOR_PAYMENT"),

		PENDING_FOR_CONNECTION_ACTIVATION("PENDING_FOR_CONNECTION_ACTIVATION"),

		CONNECTION_ACTIVATED("CONNECTION_ACTIVATED"),

		APPLIED("Applied"),

		APPROVED("Approved"),

		CANCELED("Canceled"),

		INPROGRESS("InProgress");

		private String value;

		ApplicationStatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static ApplicationStatusEnum fromValue(String text) {
			for (ApplicationStatusEnum b : ApplicationStatusEnum.values()) {
				if (String.valueOf(b.value).equals(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@JsonProperty("applicationStatus")
	private ApplicationStatusEnum applicationStatus = null;

	/**
	 * Gets or Sets status
	 */
	public enum StatusEnum {
		ACTIVE("Active"),

		INACTIVE("Inactive"),
		
	    INWORKFLOW("INWORKFLOW");

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

	@JsonProperty("status")
	private StatusEnum status = null;

	@JsonProperty("connectionNo")
	private String connectionNo = null;

	@JsonProperty("oldConnectionNo")
	private String oldConnectionNo = null;

	@JsonProperty("documents")
	@Valid
	private List<Document> documents = null;

	@JsonProperty("plumberInfo")
	@Valid
	private List<PlumberInfo> plumberInfo = null;

	@JsonProperty("roadType")
	private String roadType = null;

	@JsonProperty("roadCuttingArea")
	private Float roadCuttingArea = null;

	@NotNull
	@Size(max = 64)
	@JsonProperty("action")
	private String action = null;

	public Connection id(String id) {
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

	public Connection property(Property property) {
		this.property = property;
		return this;
	}

	/**
	 * Get property
	 * 
	 * @return property
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public Connection applicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
		return this;
	}

	/**
	 * Formatted application number, which will be generated using ID-Gen at the
	 * time .
	 * 
	 * @return applicationNo
	 **/
	@ApiModelProperty(readOnly = true, value = "Formatted application number, which will be generated using ID-Gen at the time .")

	@Size(min = 1, max = 64)
	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public Connection applicationStatus(ApplicationStatusEnum applicationStatus) {
		this.applicationStatus = applicationStatus;
		return this;
	}

	/**
	 * Get applicationStatus
	 * 
	 * @return applicationStatus
	 **/
	@ApiModelProperty(value = "")

	public ApplicationStatusEnum getApplicationStatus() {
		return applicationStatus;
	}

	public void setApplicationStatus(ApplicationStatusEnum applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public Connection status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * Get status
	 * 
	 * @return status
	 **/
	@ApiModelProperty(value = "")

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public Connection connectionNo(String connectionNo) {
		this.connectionNo = connectionNo;
		return this;
	}

	/**
	 * Formatted connection number, which will be generated using ID-Gen service
	 * after aproval of connection application in case of new application. If
	 * the source of data is \"DATA_ENTRY\" then application status will be
	 * considered as \"APROVED\" application.
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

	public Connection oldConnectionNo(String oldConnectionNo) {
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

	public Connection documents(List<Document> documents) {
		this.documents = documents;
		return this;
	}

	public Connection addDocumentsItem(Document documentsItem) {
		if (this.documents == null) {
			this.documents = new ArrayList<Document>();
		}
		if(!this.documents.contains(documentsItem))
            this.documents.add(documentsItem);
		return this;
	}

	/**
	 * The documents attached by owner for exemption.
	 * 
	 * @return documents
	 **/
	@ApiModelProperty(value = "The documents attached by owner for exemption.")
	@Valid
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	public Connection plumberInfo(List<PlumberInfo> plumberInfo) {
		this.plumberInfo = plumberInfo;
		return this;
	}

	public Connection addPlumberInfoItem(PlumberInfo plumberInfoItem) {
		if (this.plumberInfo == null) {
			this.plumberInfo = new ArrayList<PlumberInfo>();
		}
		if(!this.plumberInfo.contains(plumberInfoItem))
            this.plumberInfo.add(plumberInfoItem);
		return this;
	}

	/**
	 * The documents attached by owner for exemption.
	 * 
	 * @return plumberInfo
	 **/
	@ApiModelProperty(value = "The documents attached by owner for exemption.")
	@Valid
	public List<PlumberInfo> getPlumberInfo() {
		return plumberInfo;
	}

	public void setPlumberInfo(List<PlumberInfo> plumberInfo) {
		this.plumberInfo = plumberInfo;
	}

	public Connection roadType(String roadType) {
		this.roadType = roadType;
		return this;
	}

	/**
	 * It is a master data, defined in MDMS. If road cutting is required to
	 * established the connection then we need to capture the details of road
	 * type.
	 * 
	 * @return roadType
	 **/
	@ApiModelProperty(value = "It is a master data, defined in MDMS. If road cutting is required to established the connection then we need to capture the details of road type.")
	public String getRoadType() {
		return roadType;
	}

	public void setRoadType(String roadType) {
		this.roadType = roadType;
	}

	public Connection roadCuttingArea(Float roadCuttingArea) {
		this.roadCuttingArea = roadCuttingArea;
		return this;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Connection action(String action) {
		this.action = action;
		return this;
	}

	/**
	 * Capture the road cutting area in sqft.
	 * 
	 * @return roadCuttingArea
	 **/
	@ApiModelProperty(value = "Capture the road cutting area in sqft.")

	public Float getRoadCuttingArea() {
		return roadCuttingArea;
	}

	public void setRoadCuttingArea(Float roadCuttingArea) {
		this.roadCuttingArea = roadCuttingArea;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Connection connection = (Connection) o;
		return Objects.equals(this.id, connection.id) && Objects.equals(this.property, connection.property)
				&& Objects.equals(this.applicationNo, connection.applicationNo)
				&& Objects.equals(this.applicationStatus, connection.applicationStatus)
				&& Objects.equals(this.status, connection.status)
				&& Objects.equals(this.connectionNo, connection.connectionNo)
				&& Objects.equals(this.oldConnectionNo, connection.oldConnectionNo)
				&& Objects.equals(this.documents, connection.documents)
				&& Objects.equals(this.plumberInfo, connection.plumberInfo)
				&& Objects.equals(this.roadType, connection.roadType)
				&& Objects.equals(this.roadCuttingArea, connection.roadCuttingArea);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, property, applicationNo, applicationStatus, status, connectionNo, oldConnectionNo,
				documents, plumberInfo, roadType, roadCuttingArea);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Connection {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    property: ").append(toIndentedString(property)).append("\n");
		sb.append("    applicationNo: ").append(toIndentedString(applicationNo)).append("\n");
		sb.append("    applicationStatus: ").append(toIndentedString(applicationStatus)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    connectionNo: ").append(toIndentedString(connectionNo)).append("\n");
		sb.append("    oldConnectionNo: ").append(toIndentedString(oldConnectionNo)).append("\n");
		sb.append("    documents: ").append(toIndentedString(documents)).append("\n");
		sb.append("    plumberInfo: ").append(toIndentedString(plumberInfo)).append("\n");
		sb.append("    roadType: ").append(toIndentedString(roadType)).append("\n");
		sb.append("    roadCuttingArea: ").append(toIndentedString(roadCuttingArea)).append("\n");
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
}