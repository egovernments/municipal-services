package org.egov.wsCalculation.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * ResponseHeader should be used to carry metadata information about the
 * response from the server. apiId, ver and msgId in ResponseHeader should
 * always correspond to the same values in respective request&#x27;s
 * RequestHeader.
 */
@ApiModel(description = "ResponseHeader should be used to carry metadata information about the response from the server. apiId, ver and msgId in ResponseHeader should always correspond to the same values in respective request's RequestHeader.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-10-24T10:29:25.253+05:30[Asia/Kolkata]")
public class ResponseHeader {
	@JsonProperty("ts")
	private Long ts = null;

	@JsonProperty("resMsgId")
	private String resMsgId = null;

	@JsonProperty("msgId")
	private String msgId = null;

	/**
	 * status of request processing
	 */
	public enum StatusEnum {
		COMPLETED("COMPLETED"),

		ACCEPTED("ACCEPTED"),

		FAILED("FAILED");

		private String value;

		StatusEnum(String value) {
			this.value = value;
		}

		@Override
		@JsonValue
		public String toString() {
			return String.valueOf(value);
		}

		@JsonCreator
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

	@JsonProperty("signature")
	private String signature = null;

	@JsonProperty("error")
	private Error error = null;

	@JsonProperty("information")
	private Object information = null;

	@JsonProperty("debug")
	private Object debug = null;

	@JsonProperty("additionalInfo")
	private Object additionalInfo = null;

	public ResponseHeader ts(Long ts) {
		this.ts = ts;
		return this;
	}

	/**
	 * response time in epoch
	 * 
	 * @return ts
	 **/
	@ApiModelProperty(value = "response time in epoch")

	public Long getTs() {
		return ts;
	}

	public void setTs(Long ts) {
		this.ts = ts;
	}

	public ResponseHeader resMsgId(String resMsgId) {
		this.resMsgId = resMsgId;
		return this;
	}

	/**
	 * unique response message id (UUID) - will usually be the correlation id
	 * from the server
	 * 
	 * @return resMsgId
	 **/
	@ApiModelProperty(required = true, value = "unique response message id (UUID) - will usually be the correlation id from the server")
	@NotNull

	@Size(max = 256)
	public String getResMsgId() {
		return resMsgId;
	}

	public void setResMsgId(String resMsgId) {
		this.resMsgId = resMsgId;
	}

	public ResponseHeader msgId(String msgId) {
		this.msgId = msgId;
		return this;
	}

	/**
	 * message id of the request
	 * 
	 * @return msgId
	 **/
	@ApiModelProperty(required = true, value = "message id of the request")
	@NotNull

	@Size(max = 256)
	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public ResponseHeader status(StatusEnum status) {
		this.status = status;
		return this;
	}

	/**
	 * status of request processing
	 * 
	 * @return status
	 **/
	@ApiModelProperty(required = true, value = "status of request processing")
	@NotNull

	public StatusEnum getStatus() {
		return status;
	}

	public void setStatus(StatusEnum status) {
		this.status = status;
	}

	public ResponseHeader signature(String signature) {
		this.signature = signature;
		return this;
	}

	/**
	 * Hash describing the current ResponseHeader
	 * 
	 * @return signature
	 **/
	@ApiModelProperty(value = "Hash describing the current ResponseHeader")

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public ResponseHeader error(Error error) {
		this.error = error;
		return this;
	}

	/**
	 * Get error
	 * 
	 * @return error
	 **/
	@ApiModelProperty(value = "")

	@Valid
	public Error getError() {
		return error;
	}

	public void setError(Error error) {
		this.error = error;
	}

	public ResponseHeader information(Object information) {
		this.information = information;
		return this;
	}

	/**
	 * Additional information from API
	 * 
	 * @return information
	 **/
	@ApiModelProperty(value = "Additional information from API")

	public Object getInformation() {
		return information;
	}

	public void setInformation(Object information) {
		this.information = information;
	}

	public ResponseHeader debug(Object debug) {
		this.debug = debug;
		return this;
	}

	/**
	 * Debug information when requested
	 * 
	 * @return debug
	 **/
	@ApiModelProperty(value = "Debug information when requested")

	public Object getDebug() {
		return debug;
	}

	public void setDebug(Object debug) {
		this.debug = debug;
	}

	public ResponseHeader additionalInfo(Object additionalInfo) {
		this.additionalInfo = additionalInfo;
		return this;
	}

	/**
	 * Any additional information if required e.g. status url (to find out the
	 * current status of an asynchronous processing response), additional links
	 * to perform special functions like file uploads etc.
	 * 
	 * @return additionalInfo
	 **/
	@ApiModelProperty(value = "Any additional information if required e.g. status url (to find out the current status of an asynchronous processing response), additional links to perform special functions like file uploads etc.")

	public Object getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Object additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ResponseHeader responseHeader = (ResponseHeader) o;
		return Objects.equals(this.ts, responseHeader.ts) && Objects.equals(this.resMsgId, responseHeader.resMsgId)
				&& Objects.equals(this.msgId, responseHeader.msgId)
				&& Objects.equals(this.status, responseHeader.status)
				&& Objects.equals(this.signature, responseHeader.signature)
				&& Objects.equals(this.error, responseHeader.error)
				&& Objects.equals(this.information, responseHeader.information)
				&& Objects.equals(this.debug, responseHeader.debug)
				&& Objects.equals(this.additionalInfo, responseHeader.additionalInfo);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ts, resMsgId, msgId, status, signature, error, information, debug, additionalInfo);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ResponseHeader {\n");

		sb.append("    ts: ").append(toIndentedString(ts)).append("\n");
		sb.append("    resMsgId: ").append(toIndentedString(resMsgId)).append("\n");
		sb.append("    msgId: ").append(toIndentedString(msgId)).append("\n");
		sb.append("    status: ").append(toIndentedString(status)).append("\n");
		sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
		sb.append("    error: ").append(toIndentedString(error)).append("\n");
		sb.append("    information: ").append(toIndentedString(information)).append("\n");
		sb.append("    debug: ").append(toIndentedString(debug)).append("\n");
		sb.append("    additionalInfo: ").append(toIndentedString(additionalInfo)).append("\n");
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
