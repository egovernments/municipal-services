package org.egov.rb.pgr.v2.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;

import org.egov.rb.pgrmodels.Service;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ServiceWrapper {


    @Valid
    @NonNull
    @JsonProperty("service")
    private Service service = null;

    @Valid
    @JsonProperty("workflow")
    private Workflow workflow = null;

}
