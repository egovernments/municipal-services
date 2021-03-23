package org.egov.rb.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Metadata {
	
	@JsonProperty("nlu")
	private Nlu nlu;

}
