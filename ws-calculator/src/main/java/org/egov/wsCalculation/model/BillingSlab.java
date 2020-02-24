package org.egov.wsCalculation.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BillingSlab {
	public String id;
	public String buildingType = null;
	public String connectionType = null;
	public String calculationAttribute = null;
	public double minimumCharge;
	public List<Slab> slabs = new ArrayList<>();
}