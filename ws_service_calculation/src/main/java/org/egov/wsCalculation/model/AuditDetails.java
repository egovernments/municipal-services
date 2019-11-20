package org.egov.wsCalculation.model;



import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AuditDetails   {
        @JsonProperty("createdBy")
        private String createdBy;

        @JsonProperty("lastModifiedBy")
        private String lastModifiedBy;

        @JsonProperty("createdTime")
        private Long createdTime;

        @JsonProperty("lastModifiedTime")
        private Long lastModifiedTime;


}