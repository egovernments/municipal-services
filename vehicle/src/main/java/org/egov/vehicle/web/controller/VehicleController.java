package org.egov.vehicle.web.controller;

import org.egov.vehicle.service.VehicleService;
import org.egov.vehicle.util.ResponseInfoFactory;
import org.egov.vehicle.util.VehicleUtil;
import org.egov.vehicle.web.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v1")
public class VehicleController {
    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private VehicleUtil vehicleUtil;

    @Autowired
    private ResponseInfoFactory responseInfoFactory;

    @PostMapping(value = "/_create")
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody VehicleRequest vehicleRequest) {

        vehicleUtil.defaultJsonPathConfig();
        Vehicle vehicle = vehicleService.create(vehicleRequest);
        List<Vehicle> vehicleList = new ArrayList<Vehicle>();
        vehicleList.add(vehicle);
        VehicleResponse response = VehicleResponse.builder().vehicle(vehicleList)
                .responseInfo(responseInfoFactory.createResponseInfoFromRequestInfo(vehicleRequest.getRequestInfo(), true))
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
