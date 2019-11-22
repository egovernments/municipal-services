package org.egov.wsCalculation;

import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@ComponentScan(basePackages = { "org.egov.wsCalculation", "org.egov.wsCalculation.controller",
		"org.egov.wsCalculation.config", "org.egov.wsCalculation.repository", "org.egov.wsCalculation.service",
		"org.egov.wsCalculation.util", "org.egov.wsCalculation.producer", "org.egov.wsCalculation.consumer",
		"org.egov.wsCalculation.validator"})
public class WsCalculationApplication {
	@Value("${app.timezone}")
	private String timeZone;

	public static void main(String[] args) {
		SpringApplication.run(WsCalculationApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).setTimeZone(TimeZone.getTimeZone(timeZone));
	}
	
	
}
