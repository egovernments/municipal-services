package org.egov.wscalculation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages="org.egov.wscalculation")
public class WsCalculationApplication {

	public static void main(String[] args) {
		SpringApplication.run(WsCalculationApplication.class, args);
	}

}
