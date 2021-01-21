package org.egov.vendorregistory.web.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.common.contract.request.User;
import org.egov.vendorregistory.web.model.location.Address;
import org.egov.vendorregistory.web.model.vehicle.Vehicle;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;



/**
 * Capture the vendor information in the system.
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:34:12.238Z[GMT]")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vendor {

	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("name")
	private String name = null;

	@JsonProperty("address")
	private Address address = null;

	@JsonProperty("owner")
	private User owner = null;

	@JsonProperty("vehicles")
	@Valid
	private List<Vehicle> vehicles = new ArrayList<Vehicle>();

	@JsonProperty("drivers")
	@Valid
	private List<User> drivers = null;

	@JsonProperty("additionalDetails")
	private Object additionalDetails = null;

	@JsonProperty("source")
	private String source = null;

	@JsonProperty("description")
	private String description = null;

	/**
	 * Inactive records will be consider as soft deleted
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

	@JsonProperty("auditDetails")
	private AuditDetails auditDetails = null;

	public Vendor id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * The server generated unique ID(UUID).
	 * 
	 * @return id
	 **/
	// @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The server
	// generated unique ID(UUID).")
	@Size(min = 2, max = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vendor tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Unique identifier of the tenant.
	 * 
	 * @return tenantId
	 **/
	// @Schema(required = true, description = "Unique identifier of the tenant.")
	@NotNull
	@Size(min = 2, max = 64)
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Vendor name(String name) {
		this.name = name;
		return this;
	}

	/**
	 * Name of the vendor(business entity name).
	 * 
	 * @return name
	 **/
	@NotNull
	@Size(min = 2, max = 128)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vendor address(Address address) {
		this.address = address;
		return this;
	}

	/**
	 * Get address
	 * 
	 * @return address
	 **/
	@NotNull
	@Valid
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public Vendor owner(User owner) {
		this.owner = owner;
		return this;
	}

	/**
	 * Get owner
	 * 
	 * @return owner
	 **/
	@NotNull
	@Valid
	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Vendor vehicles(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
		return this;
	}

	public Vendor addVehiclesItem(Vehicle vehiclesItem) {
		this.vehicles.add(vehiclesItem);
		return this;
	}

	/**
	 * Get vehicles
	 * 
	 * @return vehicles
	 **/
	@NotNull
	@Valid
	public List<Vehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<Vehicle> vehicles) {
		this.vehicles = vehicles;
	}

	public Vendor drivers(List<User> drivers) {
		this.drivers = drivers;
		return this;
	}

	public Vendor addDriversItem(User driversItem) {
		if (this.drivers == null) {
			this.drivers = new ArrayList<User>();
		}
		this.drivers.add(driversItem);
		return this;
	}

	/**
	 * Get drivers
	 * 
	 * @return drivers
	 **/
	@Valid
	public List<User> getDrivers() {
		return drivers;
	}

	public void setDrivers(List<User> drivers) {
		this.drivers = drivers;
	}

	public Vendor additionalDetails(Object additionalDetails) {
		this.additionalDetails = additionalDetails;
		return this;
	}

	/**
	 * This is the json object that will carry the actual input (whereever the
	 * metadata requries input). Structure should be same as the schema definition
	 * provided in the metadata of the service (schema compliance check to be
	 * performed at client/server)
	 * 
	 * @return additionalDetail
	 **/
	// @Schema(description = "This is the json object that will carry the actual
	// input (whereever the metadata requries input). Structure should be same as
	// the schema definition provided in the metadata of the service (schema
	// compliance check to be performed at client/server)")
	public Object getAdditionalDetails() {
		return additionalDetails;
	}

	public void setAdditionalDetails(Object additionalDetails) {
		this.additionalDetails = additionalDetails;
	}

	public Vendor source(String source) {
		this.source = source;
		return this;
	}

	/**
	 * Source mdms master data. Which captures the source of the service
	 * request(ex:- whatsapp, ivr, Swachhata etc)
	 * 
	 * @return source
	 **/
	// @Schema(example = "whatsapp, ivr etc", description = "Source mdms master
	// data. Which captures the source of the service request(ex:- whatsapp, ivr,
	// Swachhata etc)")
	@Size(min = 2, max = 64)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Vendor status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * Inactive records will be consider as soft deleted
	 * 
	 * @return status
	 **/
	// @Schema(description = "Inactive records will be consider as soft deleted")
	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public Vendor auditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
		return this;
	}

	/**
	 * Get auditDetails
	 * 
	 * @return auditDetails
	 **/
	// @Schema(description = "")
	@Valid
	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	public Vendor description(String description) {
		this.description = description;
		return this;
	}

	/**
	 * Additional information or description of the application
	 * 
	 * @return description
	 **/

	@Size(min = 2, max = 256)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Vendor vendor = (Vendor) o;
		return Objects.equals(this.id, vendor.id) && Objects.equals(this.tenantId, vendor.tenantId)
				&& Objects.equals(this.name, vendor.name) && Objects.equals(this.address, vendor.address)
				&& Objects.equals(this.owner, vendor.owner) && Objects.equals(this.vehicles, vendor.vehicles)
				&& Objects.equals(this.drivers, vendor.drivers)
				&& Objects.equals(this.additionalDetails, vendor.additionalDetails)
				&& Objects.equals(this.source, vendor.source) && Objects.equals(this.status, vendor.status)
				&& Objects.equals(this.auditDetails, vendor.auditDetails)
				&& Objects.equals(this.description, vendor.description);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, tenantId, name, address, owner, vehicles, drivers, additionalDetails, source, status,
				auditDetails,description);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Vendor {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    address: ").append(toIndentedString(address)).append("\n");
		sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
		sb.append("    vehicles: ").append(toIndentedString(vehicles)).append("\n");
		sb.append("    drivers: ").append(toIndentedString(drivers)).append("\n");
		sb.append("    additionalDetails: ").append(toIndentedString(additionalDetails)).append("\n");
		sb.append("    source: ").append(toIndentedString(source)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
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
