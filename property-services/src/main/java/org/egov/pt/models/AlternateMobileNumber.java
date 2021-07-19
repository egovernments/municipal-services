package org.egov.pt.models;



import org.hibernate.validator.constraints.SafeHtml;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternateMobileNumber{
	
	@SafeHtml
	@JsonProperty("name")
	private String name;
	
	@SafeHtml
	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
}