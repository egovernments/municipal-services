package org.egov.fsm.web.model.vehicle;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VehicleRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo RequestInfo = null;

    @JsonProperty("vehicle")
    private Vehicle vehicle;

    public VehicleRequest(RequestInfo requestInfo, Vehicle vehicleForUpdate, Object o) {
    }
}
