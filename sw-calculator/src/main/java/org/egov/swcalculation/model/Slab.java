package org.egov.swcalculation.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Slab {
	public int from;
	public int to;
	public double charge;
	public double meterCharge;
}
