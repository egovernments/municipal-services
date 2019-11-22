package org.egov.waterConnection.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.egov.waterConnection.model.Address;
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
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class PropertyInfo {
	@JsonProperty("id")
	private String id = null;

	@JsonProperty("propertyId")
	private String propertyId = null;

	@JsonProperty("linkPropertyId")
	private String linkPropertyId = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("accountId")
	private String accountId = null;

	@JsonProperty("oldPropertyId")
	private String oldPropertyId = null;

	/**
	 * status of the Property
	 */
	public enum StatusEnum {
		ACTIVE("ACTIVE"),

		INACTIVE("INACTIVE");

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

	@JsonProperty("address")
	private Address address = null;

	public PropertyInfo id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * Unique Identifier of the Property for internal reference.
	 * 
	 * @return id
	 **/
	@ApiModelProperty(readOnly = true, value = "Unique Identifier of the Property for internal reference.")

	@Size(min = 1, max = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public PropertyInfo propertyId(String propertyId) {
		this.propertyId = propertyId;
		return this;
	}

	/**
	 * Unique Identifier of the Property.
	 * 
	 * @return propertyId
	 **/
	@ApiModelProperty(readOnly = true, value = "Unique Identifier of the Property.")

	@Size(min = 1, max = 64)
	public String getPropertyId() {
		return propertyId;
	}

	public void setPropertyId(String propertyId) {
		this.propertyId = propertyId;
	}

	public PropertyInfo linkPropertyId(String linkPropertyId) {
		this.linkPropertyId = linkPropertyId;
		return this;
	}

	/**
	 * Unique Identifier of the Property.
	 * 
	 * @return linkPropertyId
	 **/
	@ApiModelProperty(value = "Unique Identifier of the Property.")

	@Size(min = 1, max = 64)
	public String getLinkPropertyId() {
		return linkPropertyId;
	}

	public void setLinkPropertyId(String linkPropertyId) {
		this.linkPropertyId = linkPropertyId;
	}

	public PropertyInfo tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * tenant id of the Property
	 * 
	 * @return tenantId
	 **/
	@ApiModelProperty(required = true, value = "tenant id of the Property")
	@NotNull

	@Size(min = 2, max = 256)
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public PropertyInfo accountId(String accountId) {
		this.accountId = accountId;
		return this;
	}

	/**
	 * UUID of the user to which the property will be associated
	 * 
	 * @return accountId
	 **/
	@ApiModelProperty(readOnly = true, value = "UUID of the user to which the property will be associated")

	@Size(min = 1, max = 64)
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public PropertyInfo oldPropertyId(String oldPropertyId) {
		this.oldPropertyId = oldPropertyId;
		return this;
	}

	/**
	 * Old upic no of the Property. ULBs have the existing property in their
	 * system/manual records with their identification number, they want to
	 * continue the old reference number in this case the same identification
	 * number will be captured here.
	 * 
	 * @return oldPropertyId
	 **/
	@ApiModelProperty(value = "Old upic no of the Property. ULBs have the existing property in their system/manual records with their identification number, they want to continue the old reference number in this case the same identification number will be captured here.")

	@Size(min = 1, max = 256)
	public String getOldPropertyId() {
		return oldPropertyId;
	}

	public void setOldPropertyId(String oldPropertyId) {
		this.oldPropertyId = oldPropertyId;
	}

	public PropertyInfo status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * status of the Property
	 * 
	 * @return status
	 **/
	@ApiModelProperty(value = "status of the Property")

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public PropertyInfo address(Address address) {
		this.address = address;
		return this;
	}

	/**
	 * Get address
	 * 
	 * @return address
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	@Valid
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		PropertyInfo propertyInfo = (PropertyInfo) o;
		return Objects.equals(this.id, propertyInfo.id) && Objects.equals(this.propertyId, propertyInfo.propertyId)
				&& Objects.equals(this.linkPropertyId, propertyInfo.linkPropertyId)
				&& Objects.equals(this.tenantId, propertyInfo.tenantId)
				&& Objects.equals(this.accountId, propertyInfo.accountId)
				&& Objects.equals(this.oldPropertyId, propertyInfo.oldPropertyId)
				&& Objects.equals(this.status, propertyInfo.status)
				&& Objects.equals(this.address, propertyInfo.address);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, propertyId, linkPropertyId, tenantId, accountId, oldPropertyId, status, address);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class PropertyInfo {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    propertyId: ").append(toIndentedString(propertyId)).append("\n");
		sb.append("    linkPropertyId: ").append(toIndentedString(linkPropertyId)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    accountId: ").append(toIndentedString(accountId)).append("\n");
		sb.append("    oldPropertyId: ").append(toIndentedString(oldPropertyId)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    address: ").append(toIndentedString(address)).append("\n");
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
