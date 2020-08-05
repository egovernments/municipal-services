package org.egov.pgr.web.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestSearchCriteria {

    @NotNull
    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("serviceCode")
    private String serviceCode;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("serviceRequestId")
    private String serviceRequestId;

    @JsonProperty("ids")
    private Set<String> ids;

    @JsonIgnore
    private Set<String> userIds;

}
