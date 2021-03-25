package org.egov.rb;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
@PropertySource("classpath:application.properties")
@Import({ TracerConfiguration.class })
public class IOAdaApp
{
	//@Bean(name = "restTemplate")
	public RestTemplate restTemplate() {
		
		System.out.println("&&&&&&&&&&&&&&&&&&&&&& &&77  calling&&&&&&&&&&&&&&");
		return new RestTemplate();
	}
	public static void main(String[] args) {
	
		SpringApplication.run(IOAdaApp.class, args);
	}
	
	
}
