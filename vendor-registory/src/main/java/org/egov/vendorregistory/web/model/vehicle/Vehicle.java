package org.egov.vendorregistory.web.model.vehicle;


import java.util.Objects;

import org.egov.common.contract.request.User;
import org.egov.vendorregistory.web.model.AuditDetails;
import org.egov.vendorregistory.web.model.location.Address;
import org.egov.vendorregistory.web.model.location.Boundary;
import org.egov.vendorregistory.web.model.location.GeoLocation;
import org.springframework.validation.annotation.Validated;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Instance of Service request raised for a particular service. As per extension
 * propsed in the Service definition \&quot;attributes\&quot; carry the input
 * values requried by metadata definition in the structure as described by the
 * corresponding schema. * Any one of &#x27;address&#x27; or &#x27;(lat and
 * lang)&#x27; or &#x27;addressid&#x27; is mandatory
 */
//@Schema(description = "Instance of Service request raised for a particular service. As per extension propsed in the Service definition \"attributes\" carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of 'address' or '(lat and lang)' or 'addressid' is mandatory ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-01-06T05:37:21.257Z[GMT]")
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class Vehicle {

	@JsonProperty("vehicleOwner")
	private User vehicleOwner = null;

	@JsonProperty("id")
	private String id = null;

	@JsonProperty("tenantId")
	private String tenantId = null;

	@JsonProperty("registrationNumber")
	private String registrationNumber = null;

	@JsonProperty("model")
	private String model = null;

	@JsonProperty("type")
	private String type = null;

	@JsonProperty("tankCapicity")
	private Long tankCapicity = null;

	@JsonProperty("suctionType")
	private String suctionType = null;

	@JsonProperty("pollutionCertiValidTill")
	private Long pollutionCertiValidTill = null;

	@JsonProperty("InsuranceCertValidTill")
	private Long insuranceCertValidTill = null;

	@JsonProperty("fitnessValidTill")
	private Long fitnessValidTill = null;

	@JsonProperty("roadTaxPaidTill")
	private Long roadTaxPaidTill = null;

	@JsonProperty("gpsEnabled")
	private Boolean gpsEnabled = null;

	@JsonProperty("additionalDetail")
	private Object additionalDetail = null;

	@JsonProperty("source")
	private String source = null;

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

	public Vehicle vehicleOwner(User vehicleOwner) {
		this.vehicleOwner = vehicleOwner;
		return this;
	}

	/**
	 * Get vehicleOwner
	 * 
	 * @return vehicleOwner
	 **/
	//@Schema(description = "")
	@Valid
	public User getVehicleOwner() {
		return vehicleOwner;
	}

	public void setVehicleOwner(User vehicleOwner) {
		this.vehicleOwner = vehicleOwner;
	}

	public Vehicle id(String id) {
		this.id = id;
		return this;
	}

	/**
	 * The server generated unique ID(UUID).
	 * 
	 * @return id
	 **/
	//@Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "The server generated unique ID(UUID).")
	@Size(min = 2, max = 64)
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Vehicle tenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	/**
	 * Unique identifier of the tenant.
	 * 
	 * @return tenantId
	 **/
	//@Schema(required = true, description = "Unique identifier of the tenant.")
	@NotNull

	@Size(min = 2, max = 64)
	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	public Vehicle registrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
		return this;
	}

	/**
	 * Unique Vehicle registration number.
	 * 
	 * @return registrationNumber
	 **/
	//@Schema(required = true, description = "Unique Vehicle registration number.")
	@NotNull

	@Size(min = 2, max = 128)
	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public Vehicle model(String model) {
		this.model = model;
		return this;
	}

	/**
	 * Vehicle model number(this the master data defined in MDMS).
	 * 
	 * @return model
	 **/
	//@Schema(required = true, description = "Vehicle model number(this the master data defined in MDMS).")
	@NotNull

	@Size(min = 2, max = 256)
	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Vehicle type(String type) {
		this.type = type;
		return this;
	}

	/**
	 * Vehicle Type master, defined in MDMS.
	 * 
	 * @return type
	 **/
	//@Schema(required = true, description = "Vehicle Type master, defined in MDMS.")
	@NotNull

	@Size(min = 2, max = 64)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Vehicle tankCapicity(Long tankCapicity) {
		this.tankCapicity = tankCapicity;
		return this;
	}

	/**
	 * Tank capacity in liters.
	 * 
	 * @return tankCapicity
	 **/
	//@Schema(description = "Tank capacity in liters.")
	public Long getTankCapicity() {
		return tankCapicity;
	}

	public void setTankCapicity(Long tankCapicity) {
		this.tankCapicity = tankCapicity;
	}

	public Vehicle suctionType(String suctionType) {
		this.suctionType = suctionType;
		return this;
	}

	/**
	 * This is the mdms master data.
	 * 
	 * @return suctionType
	 **/
	//@Schema(description = "This is the mdms master data.")
	@Size(min = 2, max = 64)
	public String getSuctionType() {
		return suctionType;
	}

	public void setSuctionType(String suctionType) {
		this.suctionType = suctionType;
	}

	public Vehicle pollutionCertiValidTill(Long pollutionCertiValidTill) {
		this.pollutionCertiValidTill = pollutionCertiValidTill;
		return this;
	}

	/**
	 * Pollution certificate valid till for vehicle(epoc date).
	 * 
	 * @return pollutionCertiValidTill
	 **/
	//@Schema(description = "Pollution certificate valid till for vehicle(epoc date).")
	public Long getPollutionCertiValidTill() {
		return pollutionCertiValidTill;
	}

	public void setPollutionCertiValidTill(Long pollutionCertiValidTill) {
		this.pollutionCertiValidTill = pollutionCertiValidTill;
	}

	public Vehicle insuranceCertValidTill(Long insuranceCertValidTill) {
		this.insuranceCertValidTill = insuranceCertValidTill;
		return this;
	}

	/**
	 * Vehicle insurance valid till(epoc date).
	 * 
	 * @return insuranceCertValidTill
	 **/
	//@Schema(description = "Vehicle insurance valid till(epoc date).")
	public Long getInsuranceCertValidTill() {
		return insuranceCertValidTill;
	}

	public void setInsuranceCertValidTill(Long insuranceCertValidTill) {
		this.insuranceCertValidTill = insuranceCertValidTill;
	}

	public Vehicle fitnessValidTill(Long fitnessValidTill) {
		this.fitnessValidTill = fitnessValidTill;
		return this;
	}

	/**
	 * Vehicle fitness valid till(epoc date).
	 * 
	 * @return fitnessValidTill
	 **/
	//@Schema(description = "Vehicle fitness valid till(epoc date).")
	public Long getFitnessValidTill() {
		return fitnessValidTill;
	}

	public void setFitnessValidTill(Long fitnessValidTill) {
		this.fitnessValidTill = fitnessValidTill;
	}

	public Vehicle roadTaxPaidTill(Long roadTaxPaidTill) {
		this.roadTaxPaidTill = roadTaxPaidTill;
		return this;
	}

	/**
	 * Road tax paid for the Vehicle.
	 * 
	 * @return roadTaxPaidTill
	 **/
	//@Schema(description = "Road tax paid for the Vehicle.")
	public Long getRoadTaxPaidTill() {
		return roadTaxPaidTill;
	}

	public void setRoadTaxPaidTill(Long roadTaxPaidTill) {
		this.roadTaxPaidTill = roadTaxPaidTill;
	}

	public Vehicle gpsEnabled(Boolean gpsEnabled) {
		this.gpsEnabled = gpsEnabled;
		return this;
	}

	/**
	 * Whether Vehicle equipped with GPS device.
	 * 
	 * @return gpsEnabled
	 **/
	//@Schema(description = "Whether Vehicle equipped with GPS device.")
	public Boolean isGpsEnabled() {
		return gpsEnabled;
	}

	public void setGpsEnabled(Boolean gpsEnabled) {
		this.gpsEnabled = gpsEnabled;
	}

	public Vehicle additionalDetail(Object additionalDetail) {
		this.additionalDetail = additionalDetail;
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
	//@Schema(description = "This is the json object that will carry the actual input (whereever the metadata requries input). Structure should be same as the schema definition provided in the metadata of the service (schema compliance check to be performed at client/server)")
	public Object getAdditionalDetail() {
		return additionalDetail;
	}

	public void setAdditionalDetail(Object additionalDetail) {
		this.additionalDetail = additionalDetail;
	}

	public Vehicle source(String source) {
		this.source = source;
		return this;
	}

	/**
	 * Source mdms master data. Which captures the source of the service
	 * request(ex:- whatsapp, ivr, Swachhata etc)
	 * 
	 * @return source
	 **/
	//@Schema(example = "whatsapp, ivr etc", description = "Source mdms master data. Which captures the source of the service request(ex:- whatsapp, ivr, Swachhata etc)")
	@Size(min = 2, max = 64)
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public Vehicle status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * Inactive records will be consider as soft deleted
	 * 
	 * @return status
	 **/
	//@Schema(description = "Inactive records will be consider as soft deleted")
	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public Vehicle auditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
		return this;
	}

	/**
	 * Get auditDetails
	 * 
	 * @return auditDetails
	 **/
	//@Schema(description = "")
	@Valid
	public AuditDetails getAuditDetails() {
		return auditDetails;
	}

	public void setAuditDetails(AuditDetails auditDetails) {
		this.auditDetails = auditDetails;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Vehicle vehicle = (Vehicle) o;
		return Objects.equals(this.vehicleOwner, vehicle.vehicleOwner) && Objects.equals(this.id, vehicle.id)
				&& Objects.equals(this.tenantId, vehicle.tenantId)
				&& Objects.equals(this.registrationNumber, vehicle.registrationNumber)
				&& Objects.equals(this.model, vehicle.model) && Objects.equals(this.type, vehicle.type)
				&& Objects.equals(this.tankCapicity, vehicle.tankCapicity)
				&& Objects.equals(this.suctionType, vehicle.suctionType)
				&& Objects.equals(this.pollutionCertiValidTill, vehicle.pollutionCertiValidTill)
				&& Objects.equals(this.insuranceCertValidTill, vehicle.insuranceCertValidTill)
				&& Objects.equals(this.fitnessValidTill, vehicle.fitnessValidTill)
				&& Objects.equals(this.roadTaxPaidTill, vehicle.roadTaxPaidTill)
				&& Objects.equals(this.gpsEnabled, vehicle.gpsEnabled)
				&& Objects.equals(this.additionalDetail, vehicle.additionalDetail)
				&& Objects.equals(this.source, vehicle.source) && Objects.equals(this.status, vehicle.status)
				&& Objects.equals(this.auditDetails, vehicle.auditDetails);
	}

	@Override
	public int hashCode() {
		return Objects.hash(vehicleOwner, id, tenantId, registrationNumber, model, type, tankCapicity, suctionType,
				pollutionCertiValidTill, insuranceCertValidTill, fitnessValidTill, roadTaxPaidTill, gpsEnabled,
				additionalDetail, source, status, auditDetails);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class Vehicle {\n");

		sb.append("    vehicleOwner: ").append(toIndentedString(vehicleOwner)).append("\n");
		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
		sb.append("    registrationNumber: ").append(toIndentedString(registrationNumber)).append("\n");
		sb.append("    model: ").append(toIndentedString(model)).append("\n");
		sb.append("    type: ").append(toIndentedString(type)).append("\n");
		sb.append("    tankCapicity: ").append(toIndentedString(tankCapicity)).append("\n");
		sb.append("    suctionType: ").append(toIndentedString(suctionType)).append("\n");
		sb.append("    pollutionCertiValidTill: ").append(toIndentedString(pollutionCertiValidTill)).append("\n");
		sb.append("    insuranceCertValidTill: ").append(toIndentedString(insuranceCertValidTill)).append("\n");
		sb.append("    fitnessValidTill: ").append(toIndentedString(fitnessValidTill)).append("\n");
		sb.append("    roadTaxPaidTill: ").append(toIndentedString(roadTaxPaidTill)).append("\n");
		sb.append("    gpsEnabled: ").append(toIndentedString(gpsEnabled)).append("\n");
		sb.append("    additionalDetail: ").append(toIndentedString(additionalDetail)).append("\n");
		sb.append("    source: ").append(toIndentedString(source)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    auditDetails: ").append(toIndentedString(auditDetails)).append("\n");
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
