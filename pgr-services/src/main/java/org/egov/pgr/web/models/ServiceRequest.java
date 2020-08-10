package org.egov.pgr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Request object to fetch the report data
 */
@ApiModel(description = "Request object to fetch the report data")
@Validated
@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceRequest   {

        @NotNull
        @JsonProperty("RequestInfo")
        private RequestInfo requestInfo = null;

        @Valid
        @NotNull
        @JsonProperty("PGREntity")
        private PGREntity pgrEntity = null;


}

