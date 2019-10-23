package org.egov.pt.calculator.web.models;


import javax.validation.constraints.NotNull;

import org.egov.pt.calculator.web.models.property.AuditDetails;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * BillingSlab
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(exclude={"unitRate","arvPercent","unBuiltUnitRate","id","auditDetails" /*, "fromPlotSize", "toPlotSize", "fromFloor", "toFloor"*/})
public class MutationBillingSlab   {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("id")
    private String id;
    @NotNull
    @JsonProperty("ownerShipType")
    private String ownerShipType;
    
    @NotNull
    @JsonProperty("usageType")
    private String usageType;

    @NotNull
    @JsonProperty("areaType")
    private String areaType;

    @NotNull
    @JsonProperty("fromCurrentMarketValue")
    private BigDecimal fromCurrentMarketValue;

    @NotNull
    @JsonProperty("toCurrentMarketValue")
    private BigDecimal toCurrentMarketValue;

    @JsonProperty("cmvPercent")
    private Double cmvPercent;

    @JsonProperty("fixedAmount")
    private Double fixedAmount;

//    @NotNull
//    @JsonProperty("propertyType")
//    private String propertyType;

//    @NotNull
//    @JsonProperty("propertySubType")
//    private String propertySubType;
//    
//    @NotNull
//    @JsonProperty("ownerShipCategory")
//    private String ownerShipCategory;
//
//    @NotNull
//    @JsonProperty("subOwnerShipCategory")
//    private String subOwnerShipCategory;
//
//   
//    @NotNull
//    @JsonProperty("occupancyType")
//    private String occupancyType;
//
//    @JsonProperty("arvPercent")
//    private Double arvPercent;
        
    @JsonProperty("auditDetails")
    private AuditDetails auditDetails;
}

