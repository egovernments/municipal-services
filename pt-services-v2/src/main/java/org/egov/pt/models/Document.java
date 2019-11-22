package org.egov.pt.models;

import javax.validation.constraints.NotNull;

import org.egov.pt.models.enums.DocumentBelongsTo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document {
	
        @JsonProperty("id")
        private String id ;
        
        @JsonProperty("tenantId")
        @NotNull
        private String tenantId ;
        
        @JsonProperty("entityId")
        private String entityId;

        @JsonProperty("documentType")
        @NotNull
        private String documentType ;
        
        @JsonProperty("documentBelongsTo")
        @NotNull
        private DocumentBelongsTo documentBelongsTo ;

        @JsonProperty("fileStore")
        @NotNull
        private String fileStore ;
        
        @JsonProperty("active")
        private Boolean active;

        @JsonProperty("documentUid")
        @NotNull
        private String documentUid ;
}

