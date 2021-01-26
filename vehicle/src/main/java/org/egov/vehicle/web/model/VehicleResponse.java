package org.egov.vehicle.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.response.ResponseInfo;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleResponse {

    @JsonProperty("responseInfo")
    private ResponseInfo responseInfo = null;

    @JsonProperty("vehicle")
    private List<Vehicle> vehicle = null;
}
