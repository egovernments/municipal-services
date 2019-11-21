package org.egov.waterConnection.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.egov.waterConnection.model.Error;
import org.egov.waterConnection.model.ResponseHeader;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * All APIs will return ErrorRes in case of failure which will carry
 * ResponseHeader as metadata and Error object as actual representation of
 * error. In case of bulk apis, some apis may chose to return the array of Error
 * objects to indicate individual failure.
 */
@ApiModel(description = "All APIs will return ErrorRes in case of failure which will carry ResponseHeader as metadata and Error object as actual representation of error. In case of bulk apis, some apis may chose to return the array of Error objects to indicate individual failure.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class ErrorRes {
	@JsonProperty("ResponseHeader")
	private ResponseHeader responseHeader = null;

	@JsonProperty("Errors")
	@Valid
	private List<Error> errors = null;

	public ErrorRes responseHeader(ResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
		return this;
	}

	/**
	 * Get responseHeader
	 * 
	 * @return responseHeader
	 **/
	@ApiModelProperty(required = true, value = "")
	@NotNull

	@Valid
	public ResponseHeader getResponseHeader() {
		return responseHeader;
	}

	public void setResponseHeader(ResponseHeader responseHeader) {
		this.responseHeader = responseHeader;
	}

	public ErrorRes errors(List<Error> errors) {
		this.errors = errors;
		return this;
	}

	public ErrorRes addErrorsItem(Error errorsItem) {
		if (this.errors == null) {
			this.errors = new ArrayList<Error>();
		}
		this.errors.add(errorsItem);
		return this;
	}

	/**
	 * Error response array corresponding to Request Object array. In case of
	 * single object submission or _search related paths this may be an array of
	 * one error element
	 * 
	 * @return errors
	 **/
	@ApiModelProperty(value = "Error response array corresponding to Request Object array. In case of single object submission or _search related paths this may be an array of one error element")
	@Valid
	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ErrorRes errorRes = (ErrorRes) o;
		return Objects.equals(this.responseHeader, errorRes.responseHeader)
				&& Objects.equals(this.errors, errorRes.errors);
	}

	@Override
	public int hashCode() {
		return Objects.hash(responseHeader, errors);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ErrorRes {\n");

		sb.append("    responseHeader: ").append(toIndentedString(responseHeader)).append("\n");
		sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
