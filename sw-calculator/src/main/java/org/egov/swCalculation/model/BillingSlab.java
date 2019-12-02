package org.egov.swCalculation.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingSlab {
	public String id;
	public String BuildingType = null;
	public String ConnectionType = null;
	public String CalculationAttribute = null;
	public String UOM = null;
	public List<Slab> slabs = new ArrayList<>();
}
