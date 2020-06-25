package org.egov.tl.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TradeLicenseSearchCriteria {


    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("applicationType")
    private String applicationType;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("applicationNumber")
    private String applicationNumber;

    @JsonProperty("licenseNumbers")
    private List<String> licenseNumbers;

    @JsonProperty("oldLicenseNumber")
    private String oldLicenseNumber;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonIgnore
    private String accountId;


    @JsonProperty("fromDate")
    private Long fromDate = null;

    @JsonProperty("toDate")
    private Long toDate = null;

    @JsonProperty("businessService")
    private String businessService = null;

    @JsonProperty("validTo")
    private Long validTo = null;

    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonIgnore
    private List<String> ownerIds;


    public boolean isEmpty() {
        return (this.tenantId == null && this.status == null && this.applicationType == null && this.ids == null && this.applicationNumber == null
                && this.licenseNumbers == null && this.oldLicenseNumber == null && this.mobileNumber == null &&
                this.fromDate == null && this.toDate == null && this.ownerIds == null
        );
    }

    public boolean tenantIdOnly() {
        return (this.tenantId != null && this.status == null && this.applicationType == null && this.ids == null && this.applicationNumber == null
                && this.licenseNumbers == null && this.oldLicenseNumber == null && this.mobileNumber == null &&
                this.fromDate == null && this.toDate == null && this.ownerIds == null
        );
    }

}
