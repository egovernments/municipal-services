package org.egov.waterConnection.model;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
/**
 * 
 * Meter Reading
 *
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
public class MeterReading {
	
	@JsonProperty("billingPeriod")
	public String billingPeriod;
	
	@JsonProperty("meterStatus")
	public String meterStatus;

	@JsonProperty("lastReading")
	public int lastReading;

	@JsonProperty("lastReadingDate")
	public Long lastReadingDate;
	
	@JsonProperty("currentReading")
	public int currentReading;
	
	@JsonProperty("currentReadingDate")
	public Long currentReadingDate;
	
	@JsonProperty("consumption")
	public int consumption;
}
