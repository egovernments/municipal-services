package org.egov.bpa.web.models;

import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@Validated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OwnerInfo {

	@NotNull
	@JsonProperty("id")
	private String id;
	
	@NotNull
	@JsonProperty("name")
	private String name;
	
	@NotNull
	@JsonProperty("mobileNumber")
	private String mobileNumber;
	
	@NotNull
	@JsonProperty("gender")
	private String gender;
	
	@NotNull
	@JsonProperty("fatherOrHusbandName")
	private String fatherOrHusbandName;
	
	@JsonProperty("correspondenceAddress")
	private String correspondenceAddress;
	
	@JsonProperty("isPrimaryOwner")
	private boolean isPrimaryOwner;
	
	@JsonProperty("ownerShipPercentage")
	private int ownerShipPercentage;
	
	@JsonProperty("ownerType")
	private String ownerType;
	
	@JsonProperty("institutionId")
	private String institutionId;
	
	@JsonProperty("documents")
	private Set<Document> documents;

	public enum RelationshipEnum {
		FATHER("FATHER"),

		HUSBAND("HUSBAND");

		private String value;

		RelationshipEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
		public static RelationshipEnum fromValue(String text) {
			for (RelationshipEnum b : RelationshipEnum.values()) {
				if (String.valueOf(b.value).equalsIgnoreCase(text)) {
					return b;
				}
			}
			return null;
		}
	}

	@NotNull
	@JsonProperty("relationship")
	private RelationshipEnum relationship;

	private Object additionalDetails;
}
