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
public class BillingSlab   {
	
		@NotNull
        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("id")
        private String id;

        @NotNull
        @JsonProperty("propertyType")
        private String propertyType;


        @NotNull
        @JsonProperty("usageCategory")
        private String usageCategory;

        @NotNull
        @JsonProperty("ownerShipCategory")
        private String ownerShipCategory;

        @NotNull
        @JsonProperty("areaType")
        private String areaType;

        @NotNull
        @JsonProperty("fromPlotSize")
        private Double fromPlotSize;

        @NotNull
        @JsonProperty("toPlotSize")
        private Double toPlotSize;
        
        @NotNull
        @JsonProperty("occupancyType")
        private String occupancyType;
        
        @NotNull
        @JsonProperty("fromFloor")
        private Double fromFloor;

        @NotNull
        @JsonProperty("toFloor")
        private Double toFloor;

        @JsonProperty("unitRate")
        private Double unitRate;
        
        @NotNull
        @JsonProperty("isPropertyMultiFloored")
        private Boolean isPropertyMultiFloored;
        
        @JsonProperty("unBuiltUnitRate")
        private Double unBuiltUnitRate;
        
        @JsonProperty("arvPercent")
        private Double arvPercent;
        
        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
}

