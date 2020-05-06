package org.egov.bpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.egov.land")
public class BPAApplication {

	public static void main(String[] args) {
		SpringApplication.run(BPAApplication.class, args);
	}

}
