package org.egov.bpa.calculator.web.models.bpa;

import java.util.List;

import javax.validation.Valid;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class BPAResponse {

	 @JsonProperty("ResponseInfo")
     private ResponseInfo responseInfo;

     @JsonProperty("Bpa")
     @Valid
     private List<BPA> bpa;

}
