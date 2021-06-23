package org.egov;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ TracerConfiguration.class })
public class FSMApplication {

	public static void main(String[] args) {
		SpringApplication.run(FSMApplication.class, args);
	}

}
