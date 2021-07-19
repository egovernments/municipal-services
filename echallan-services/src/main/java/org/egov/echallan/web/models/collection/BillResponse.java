package org.egov.echallan.web.models.collection;

import java.util.ArrayList;
import java.util.List;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BillResponse
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {

	@JsonProperty("ResponseInfo")
	private ResponseInfo responseInfo = null;

	@JsonProperty("Bill")
	private List<Bill> bill = new ArrayList<>();

}
