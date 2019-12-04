package org.egov.wsCalculation.config;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.builders.PathSelectors.regex;

@EnableSwagger2
@Configuration
public class SwaggerConfig {
	@Bean
	public Docket productApi() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage("org.egov.wsCalculation.controller"))
				.paths(regex("/waterCalculator.*")).build();
	}

//	private ApiInfo metaInfo() { ApiInfo apiInfo = new ApiInfo(
//            "Spring Boot Swagger Example API",
//            "Spring Boot Swagger Example API for Youtube",
//            "1.0",
//            "Terms of Service",
//            new Contact("TechPrimers", "https://www.youtube.com/TechPrimers",
//                    "techprimerschannel@gmail.com"),
//            "Apache License Version 2.0",
//            "https://www.apache.org/licesen.html"
//    );
//	  return apiInfo;
//	}
}
