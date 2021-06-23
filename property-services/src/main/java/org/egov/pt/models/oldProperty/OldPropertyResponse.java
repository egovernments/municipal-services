package org.egov.pt.models.oldProperty;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contract class to send response. Array of Property items  are used in case of search results or response for create. Where as single Property item is used for update
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OldPropertyResponse {
        @JsonProperty("responseInfo")
        private ResponseInfo responseInfo;

        @JsonProperty("properties")
        @Valid
        private List<OldProperty> properties;


        public OldPropertyResponse addPropertiesItem(OldProperty propertiesItem) {
            if (this.properties == null) {
            this.properties = new ArrayList<>();
            }
        this.properties.add(propertiesItem);
        return this;
        }

}

