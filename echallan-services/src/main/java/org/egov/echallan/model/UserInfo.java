/*
 * eChallan System
 * ### API Specs For eChallan System ### 1. Generate the new challan. 2. Update the details of existing challan 3. Search the existing challan 4. Generate the demand and bill for the challan amount so that collection can be done in online and offline mode. 
 *
 * OpenAPI spec version: 1.0.0
 * Contact: contact@egovernments.org
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package org.egov.echallan.model;


import org.egov.echallan.web.models.user.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import org.egov.common.contract.request.Role;
import org.hibernate.validator.constraints.SafeHtml;

/**
 * This is acting ID token of the authenticated user on the server. Any value provided by the clients will be ignored and actual user based on authtoken will be used on the server.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2020-08-10T16:46:24.044+05:30[Asia/Calcutta]")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo extends User {

  @JsonProperty("tenantId")
  @SafeHtml
  private String tenantId;

  @JsonProperty("uuid")
  @SafeHtml
  private String uuid;

  @JsonProperty("userName")
  @SafeHtml
  private String userName;

  @JsonProperty("password")
  @SafeHtml
  private String password;

  @JsonProperty("idToken")
  @SafeHtml
  private String idToken;

  @JsonProperty("mobileNumber")
  @SafeHtml
  private String mobileNumber;

  @JsonProperty("email")
  @SafeHtml
  private String email;

  @JsonProperty("primaryrole")
  private List<Role> primaryrole = new ArrayList<Role>();

  @JsonProperty("additionalroles")

  private List<TenantRole> additionalroles;
  public UserInfo tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

}
