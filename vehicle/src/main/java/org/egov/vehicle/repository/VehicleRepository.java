package org.egov.vehicle.repository;
import org.egov.vehicle.config.VehicleConfiguration;
import org.egov.vehicle.producer.VehicleProducer;
import org.egov.vehicle.web.model.VehicleRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleRepository {

        @Autowired
        private VehicleConfiguration config;

        @Autowired
        private VehicleProducer vehicleProducer;

        public void save(VehicleRequest vehicleRequest) {
            vehicleProducer.push(config.getSaveTopic(), vehicleRequest);
        }



}
