package org.egov.vehicle.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Vehicle {

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("registrationNumber")
    private String registrationNumber  = null;

    @JsonProperty("model")
    private String model = null;

    @JsonProperty("type")
    private String type = null;

    @JsonProperty("tankCapicity")
    private int tankCapicity;
    @JsonProperty("suctionType")
    private String suctionType = null;

    @JsonProperty("pollutionCertiValidTill")
    private int pollutionCertiValidTill;

    @JsonProperty("InsuranceCertValidTill")
    private int InsuranceCertValidTill;

    @JsonProperty("fitnessValidTill")
    private int fitnessValidTill;

    @JsonProperty("roadTaxPaidTill")
    private int  roadTaxPaidTill;

    @JsonProperty("gpsEnabled")
    private boolean gpsEnabled;

    @JsonProperty("additionalDetails")
    private Object additionalDetails = null;

    @JsonProperty("source")
    private String source = null;

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

    @JsonProperty("owner_id")
    private String owner_id = null;

    @JsonProperty("auditDetails")
    private AuditDetails auditDetails = null;

    public Vehicle id(String id) {
        this.id = id;
        return this;
    }

    @Size(min=2,max=64)   public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Vehicle tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Size(min=2,max=64)   public String getTenantId() {
        return tenantId;
    }

    public void setTenantId() {
        this.tenantId = tenantId;
    }

    public Vehicle registrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        return this;
    }

    @Size(min=2,max=128)   public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    @Size(min=2,max=256) public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Vehicle model(String model) {
        this.model = model;
        return this;
    }

    @Size(min=2,max=64)  public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Vehicle type(String type) {
        this.type = type;
        return this;
    }

    @Size(min=2,max=64)   public int getTankCapicity() {
        return tankCapicity;
    }

    public void setTankCapicity(int tankCapicity) {
        this.tankCapicity = tankCapicity;
    }

    public Vehicle tankCapicity(int tankCapicity) {
        this.tankCapicity = tankCapicity;
        return this;
    }

    @Size(min=2,max=64) public String getSuctionType() {
        return suctionType;
    }

    public void setSuctionType(String suctionType) {
        this.suctionType = suctionType;
    }

    public Vehicle suctionType(String suctionType) {
        this.suctionType = suctionType;
        return this;
    }

    @Size(min=2,max=64)  public int getPollutionCertiValidTill() {
        return pollutionCertiValidTill;
    }

    public void setPollutionCertiValidTill(int pollutionCertiValidTill) {
        this.pollutionCertiValidTill = pollutionCertiValidTill;
    }

    public Vehicle pollutionCertiValidTill(int pollutionCertiValidTill) {
        this.pollutionCertiValidTill = pollutionCertiValidTill;
        return this;
    }

    @Size(min=2,max=64) public int getInsuranceCertValidTill() {
        return InsuranceCertValidTill;
    }

    public void setInsuranceCertValidTill(int insuranceCertValidTill) {
        InsuranceCertValidTill = insuranceCertValidTill;
    }

    public Vehicle InsuranceCertValidTill(int insuranceCertValidTill) {
        this.InsuranceCertValidTill = insuranceCertValidTill;
        return this;
    }

    @Size(min=2,max=64) public int getFitnessValidTill() {
        return fitnessValidTill;
    }

    public void setFitnessValidTill(int fitnessValidTill) {
        this.fitnessValidTill = fitnessValidTill;
    }

    public Vehicle fitnessValidTill(int fitnessValidTill) {
        this.fitnessValidTill = fitnessValidTill;
        return this;
    }

    @Size(min=2,max=64) public int getRoadTaxPaidTill() {
        return roadTaxPaidTill;
    }

    public void setRoadTaxPaidTill(int roadTaxPaidTill) {
        this.roadTaxPaidTill = roadTaxPaidTill;
    }

    public Vehicle roadTaxPaidTill(int roadTaxPaidTill) {
        this.roadTaxPaidTill = roadTaxPaidTill;
        return this;
    }

    public boolean isGpsEnabled() {
        return gpsEnabled;
    }

    public void setGpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
    }

    public Vehicle gpsEnabled(boolean gpsEnabled) {
        this.gpsEnabled = gpsEnabled;
        return this;
    }

    public Object getAdditionalDetails() {
        return additionalDetails;
    }

    public void setAdditionalDetails(Object additionalDetails) {
        this.additionalDetails = additionalDetails;
    }

    public Vehicle additionalDetails(Object additionalDetails) {
        this.additionalDetails = additionalDetails;
        return this;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Vehicle source(String source)
    {
        this.source=source;
        return this;
    }

    public Vehicle status(StatusEnum status) {
        this.status = status;
        return this;
    }

    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public void setOwner_id(String owner_id) {
        this.owner_id = owner_id;
    }

    public Vehicle owner_id(String owner_id)
    {
        this.owner_id=owner_id;
        return this;
    }

    public Vehicle auditDetails(AuditDetails auditDetails) {
        this.auditDetails = auditDetails;
        return this;
    }
    @Valid
    public AuditDetails getAuditDetails() {
        return auditDetails;
    }

    public void setAuditDetails(AuditDetails auditDetails) {
        this.auditDetails = auditDetails;
    }

}


