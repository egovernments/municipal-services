package org.egov.pt.calculator.web.models.property;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This is lightweight property object that can be used as reference by definitions needing property linking. Actual Property Object extends this to include more elaborate attributes of the property.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyInfo   {

	@JsonProperty("id")
	private String id;

	@JsonProperty("propertyId")
	private String propertyId;

	@JsonProperty("tenantId")
	private String tenantId;

	@JsonProperty("accountId")
	private String accountId;

	@JsonProperty("oldPropertyId")
	private String oldPropertyId;

	@JsonProperty("status")
	private Status status;

	@JsonProperty("address")
	@NotNull
	private Address address;

	@JsonProperty("parentProperties")
	private List<String> parentProperties;
	
	
	public enum Status {
		  
		  ACTIVE("ACTIVE"),
		  
		  INACTIVE("INACTIVE");

		  private String value;

		  Status(String value) {
		    this.value = value;
		  }

		  @Override
		  @JsonValue
		  public String toString() {
		    return String.valueOf(value);
		  }

		  @JsonCreator
		  public static Status fromValue(String text) {
		    for (Status b : Status.values()) {
		      if (String.valueOf(b.value).equalsIgnoreCase(text)) {
		        return b;
		      }
		    }
		    return null;
		  }
		}
}

