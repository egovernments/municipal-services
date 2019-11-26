package org.egov.bpa.web.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Validated
public class BPARequest {

	@JsonProperty("RequestInfo")
	private RequestInfo requestInfo;

	@JsonProperty("BPA")
	private BPA BPA;

}
