package org.egov.vehicle.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.vehicle.web.model.VehicleRequest;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
public class EnrichmentService {

    public void enrichVehicleCreateRequest(VehicleRequest vehicleRequest) {
        RequestInfo requestInfo = vehicleRequest.getRequestInfo();
        vehicleRequest.getVehicle().setId(UUID.randomUUID().toString());
    }


}
