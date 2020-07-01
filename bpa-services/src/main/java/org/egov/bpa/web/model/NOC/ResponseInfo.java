package org.egov.bpa.web.model.NOC;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseInfo {
	 @JsonProperty("apiId")
	  private String apiId = null;

	  @JsonProperty("ver")
	  private String ver = null;

	  @JsonProperty("ts")
	  private Long ts = null;

	  @JsonProperty("resMsgId")
	  private String resMsgId = null;

	  @JsonProperty("msgId")
	  private String msgId = null;

	  /**
	   * status of request processing - to be enhanced in futuer to include INPROGRESS
	   */
//	  @JsonAdapter(StatusEnum.Adapter.class)
	  public enum StatusEnum {
	    SUCCESSFUL("SUCCESSFUL"),
	    FAILED("FAILED");

	    private String value;

	    StatusEnum(String value) {
	      this.value = value;
	    }
	    public String getValue() {
	      return value;
	    }

	    @Override
	    public String toString() {
	      return String.valueOf(value);
	    }
	    public static StatusEnum fromValue(String text) {
	      for (StatusEnum b : StatusEnum.values()) {
	        if (String.valueOf(b.value).equals(text)) {
	          return b;
	        }
	      }
	      return null;
	    }	
	  }  
	  
	  @JsonProperty("status")
	  private StatusEnum status = null;

}
