package org.egov.rb.model;

import java.util.List;

import org.egov.common.contract.request.RequestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageRequest {
	
	@JsonProperty("RequestInfo")
	  private RequestInfo requestInfo = null;
 
	@Autowired
	@JsonProperty("contacts")
	private List<Contacts> contacts;
	//@Autowired
	@JsonProperty("messages")
	private List<Messages> messages;
	//@Autowired
	@JsonProperty("thread")
	private ThreadContact threadContact;
	
}
