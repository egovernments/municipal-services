package org.egov.vehicle.web.model;

import javax.validation.constraints.Size;

import org.egov.vehicle.web.model.user.User;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

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

    @JsonProperty("id")
    private String id = null;

	@Size(max=64)
    @JsonProperty("tenantId")
    private String tenantId = null;

    @NonNull
	@Size(max=64)
    @JsonProperty("registrationNumber")
    private String registrationNumber  = null;

    @NonNull
	@Size(max=64)
    @JsonProperty("model")
    private String model = null;

    @NonNull
	@Size(max=64)
    @JsonProperty("type")
    private String type = null;


    @JsonProperty("tankCapacity")
    private Double tankCapacity;
    
    @JsonProperty("suctionType")
    private String suctionType = null;

    @JsonProperty("pollutionCertiValidTill")
    private Long pollutionCertiValidTill;

    @JsonProperty("InsuranceCertValidTill")
    private Long InsuranceCertValidTill;

    @JsonProperty("fitnessValidTill")
    private Long fitnessValidTill;

    @JsonProperty("roadTaxPaidTill")
    private Long  roadTaxPaidTill;

    @JsonProperty("gpsEnabled")
    private boolean gpsEnabled;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("source")
    private String source = null;
    
    @JsonProperty("ownerId")
    private String ownerId = null;

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

    @JsonProperty("owner")
    private User owner = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    

}


