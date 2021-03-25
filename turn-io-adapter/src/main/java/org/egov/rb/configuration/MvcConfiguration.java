package org.egov.rb.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class MvcConfiguration {
	
	@Bean
	public RestTemplate restTemplate() {
		System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&77  calling&&&&&&&&&&&&&&");
		return new RestTemplate();
	}

}
