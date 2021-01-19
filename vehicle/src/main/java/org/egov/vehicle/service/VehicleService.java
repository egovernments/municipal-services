package org.egov.vehicle.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.vehicle.repository.VehicleRepository;
import org.egov.vehicle.util.VehicleUtil;
import org.egov.vehicle.web.model.Vehicle;
import org.egov.vehicle.web.model.VehicleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class VehicleService {

    @Autowired
    private VehicleUtil util;

    @Autowired
    private EnrichmentService enrichmentService;

    @Autowired
    private VehicleRepository repository;

    public Vehicle create(VehicleRequest vehicleRequest) {
        enrichmentService.enrichVehicleCreateRequest(vehicleRequest);
        repository.save(vehicleRequest);
        return vehicleRequest.getVehicle();
    }

}
