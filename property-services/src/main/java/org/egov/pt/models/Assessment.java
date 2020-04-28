package org.egov.pt.models;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.pt.models.enums.Channel;
import org.egov.pt.models.enums.Status;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Assessment {
	
        @JsonProperty("financialYear")
        @NotNull
        private String financialYear ;

        @JsonProperty("assessmentNumber")
        private String assessmentNumber ;

        @JsonProperty("id")
        private String id ;
        
        @JsonProperty("tenantId")
        @NotNull
        private String tenantId ;
        
        @JsonProperty("propertyID")
        @NotNull
        private String propertyID;

        @JsonProperty("assessmentDate")
        @NotNull
        private Long assessmentDate ;

        @JsonProperty("status")
        private Status status ;

        @JsonProperty("source")
        @NotNull
        private Source source ;

        @JsonProperty("buildUpArea")
        private Double buildUpArea ;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails ;

        @JsonProperty("units")
        @Valid
        private List<Unit> units ;

        @JsonProperty("documents")
        @Valid
        private List<Document> documents ;

        @JsonProperty("additionalDetails")
        private JsonNode additionalDetails ;

        @JsonProperty("channel")
        private Channel channel ;
        
        public enum Source {
        	  
        	  MUNICIPAL_RECORDS("MUNICIPAL_RECORDS"),
        	  
        	  WEBAPP("WEBAPP"),

        	  MOBILEAPP("MOBILEAPP"),

        	  FIELD_SURVEY("FIELD_SURVEY");

        	  private String value;

        	  Source(String value) {
        	    this.value = value;
        	  }

        	  @Override
        	  @JsonValue
        	  public String toString() {
        	    return String.valueOf(value);
        	  }

        	  @JsonCreator
        	  public static Source fromValue(String text) {
        	    for (Source b : Source.values()) {
        	      if (String.valueOf(b.value).equalsIgnoreCase(text)) {
        	        return b;
        	      }
        	    }
        	    return null;
        	  }
        	}

        public Assessment addUnitsItem(Unit unitsItem) {
            if (this.units == null) {
            this.units = new ArrayList<>();
            }
        this.units.add(unitsItem);
        return this;
        }

        public Assessment addDocumentsItem(Document documentsItem) {
            if (this.documents == null) {
            this.documents = new ArrayList<>();
            }
        this.documents.add(documentsItem);
        return this;
        }

}

