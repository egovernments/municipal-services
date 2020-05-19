package org.egov.bpa.calculator.web.models.landinfo;

public enum Relationship {
	FATHER("FATHER"), HUSBAND("HUSBAND");

	private String value;

	Relationship(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public static Relationship fromValue(String text) {
		for (Relationship b : Relationship.values()) {
			if (String.valueOf(b.value).equals(text)) {
				return b;
			}
		}
		return null;
	}
}
