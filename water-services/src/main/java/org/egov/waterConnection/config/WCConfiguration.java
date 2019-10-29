package org.egov.waterConnection.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Component

public class WCConfiguration {

	@Value("${egov.waterservice.pagination.default.limit}")
	private Long defaultLimit;

	@Value("${egov.waterservice.pagination.default.offset}")
	private Long defaultOffset;

}
