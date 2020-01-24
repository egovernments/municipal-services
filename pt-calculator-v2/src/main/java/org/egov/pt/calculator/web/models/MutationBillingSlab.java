package org.egov.pt.calculator.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.pt.calculator.web.models.property.AuditDetails;

import javax.validation.constraints.NotNull;

/**
 * Mutation BillingSlab
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
public class MutationBillingSlab   {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("id")
    private String id;

    @JsonProperty("propertyType")
    private String propertyType;

    @JsonProperty("propertySubType")
    private String propertySubType;

    @NotNull
    @JsonProperty("usageCategoryMajor")
    private String usageCategoryMajor;

    @JsonProperty("usageCategoryMinor")
    private String usageCategoryMinor;

    @JsonProperty("usageCategorySubMinor")
    private String usageCategorySubMinor;

    @JsonProperty("usageCategoryDetail")
    private String usageCategoryDetail;

    @JsonProperty("ownerShipCategory")
    private String ownerShipCategory;

    @JsonProperty("subOwnerShipCategory")
    private String subOwnerShipCategory;

    @JsonProperty("minMarketValue")
    private Double minMarketValue;

    @JsonProperty("maxMarketValue")
    private Double maxMarketValue;

    @NotNull
    @JsonProperty("fixedAmount")
    private Double fixedAmount;

}


