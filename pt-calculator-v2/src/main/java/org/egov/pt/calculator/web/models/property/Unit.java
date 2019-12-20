package org.egov.pt.calculator.web.models.property;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

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

        @JsonProperty("floorNo")
        private String floorNo;

        @JsonProperty("unitArea")
        @NotNull
        private Double unitArea;

        @JsonProperty("usageCategory")
        @NotNull
        private String usageCategory;

        @JsonProperty("occupancyType")
        @NotNull
        private OccupancyType occupancyType;

        @JsonProperty("occupancyDate")
        @NotNull
        private Long occupancyDate;

        @JsonProperty("constructionType")
        @NotNull
        private String constructionType;
        
        @JsonProperty("active")
        private Boolean active;

        @JsonProperty("arv")
        private BigDecimal arv;
        
        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;
        
        
        public enum OccupancyType {
        	  
        	  OWNER("OWNER"),
        	  
        	  TENANT("TENANT");

        	  private String value;

        	  OccupancyType(String value) {
        	    this.value = value;
        	  }

        	  @Override
        	  @JsonValue
        	  public String toString() {
        	    return String.valueOf(value);
        	  }

        	  @JsonCreator
        	  public static OccupancyType fromValue(String text) {
        	    for (OccupancyType b : OccupancyType.values()) {
        	      if (String.valueOf(b.value).equals(text)) {
        	        return b;
        	      }
        	    }
        	    return null;
        	  }
        	}


}

