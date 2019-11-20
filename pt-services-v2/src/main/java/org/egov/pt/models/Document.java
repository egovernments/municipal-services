package org.egov.pt.models;

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

        @JsonProperty("documentType")
        private String documentType ;

        @JsonProperty("fileStore")
        private String fileStore ;

        @JsonProperty("documentUid")
        private String documentUid ;
}

