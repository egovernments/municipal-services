package org.egov.wsCalculation.model;

import java.util.ArrayList;
import java.util.List;


public class BillingSlab {
	public String BuildingType = null;
	public String ConnectionType = null;
	public String CalculationAttribute = null;
	public String UOM = null;
	public List<Slab> slabs = new ArrayList<>();
}