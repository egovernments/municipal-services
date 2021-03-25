package org.egov.rb.contract;

import java.util.ArrayList;

import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.egov.common.contract.response.ResponseInfo;
import org.egov.rb.model.Contact;
import org.egov.rb.model.Contacts;
import org.egov.rb.model.Messages;
import org.egov.rb.model.ThreadContact;
import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Response to the service request
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2018-03-23T08:00:37.661Z")

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ServiceResponse   {
	@JsonProperty("ResponseInfo")
	  private ResponseInfo responseInfo = null;


		@JsonProperty("contacts")
		private List<Contacts> contacts;

		@JsonProperty("messages")
		private List<Messages> messages;
		
		@JsonProperty("thread")
		private ThreadContact threadContact;
		
	
}
	