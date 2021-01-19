package org.egov.vehicle.config;
import lombok.*;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Component
public class VehicleConfiguration {

    // Persister Config
    @Value("${persister.save.vehicle.topic}")
    private String saveTopic;

}
