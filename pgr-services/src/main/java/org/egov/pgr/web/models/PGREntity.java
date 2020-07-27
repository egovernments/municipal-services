package org.egov.pgr.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.Valid;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PGREntity {


    @Valid
    @NonNull
    @JsonProperty("service")
    private Service service = null;

    @Valid
    @NonNull
    @JsonProperty("workflow")
    private Workflow workflow = null;

}
