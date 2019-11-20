package org.egov.pt.models;

import org.egov.pt.models.enums.OccupancyType;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unit
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Unit   {
        @JsonProperty("id")
        private String id;

        @JsonProperty("tenantId")
        private String tenantId;

        @JsonProperty("floorNo")
        private String floorNo;

        @JsonProperty("unitArea")
        private Double unitArea;

        @JsonProperty("usageCategory")
        private String usageCategory;

        @JsonProperty("occupancyType")
        private OccupancyType occupancyType;

        @JsonProperty("occupancyDate")
        private Long occupancyDate;

        @JsonProperty("constructionType")
        private String constructionType;

        @JsonProperty("arv")
        private Double arv;


}

