package org.egov.bpa.web.model;

public enum Source {
	MUNICIPAL_RECORDS("MUNICIPAL_RECORDS"),
	  FIELD_SURVEY("FIELD_SURVEY"), 
	  WEBAPP("WEBAPP");

	  private String value;

	  Source(String value) {
	    this.value = value;
	  }

	  public String getValue() {
	    return value;
	  }

	  @Override
	  public String toString() {
	    return String.valueOf(value);
	  }

	  public static Source fromValue(String text) {
	    for (Source b : Source.values()) {
	      if (String.valueOf(b.value).equals(text)) {
	        return b;
	      }
	    }
	    return null;
	  }

}
