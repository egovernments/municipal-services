package org.egov.rb.model.user;

import java.util.List;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Citizen {

	private Long id=(long) 789090;
	private String uuid="3f9a133a-2534-4ad6-86eb-984c2080f8ec";
	
	@Pattern(regexp="^[a-zA-Z. ]*$")
	@Size(max=30)
	private String name="ram";
	
	@JsonProperty("permanentAddress")
	@Pattern(regexp = "^[a-zA-Z0-9!@#.,/: ()&'-]*$")
	@Size(max=160)
	private String address="amruthsar";
	
	@Pattern(regexp="(^$|[0-9]{10})")
	private String mobileNumber="9080909090";
	
	private String aadhaarNumber="478709876789";
	private String pan="acs5677";
	
	@Email
	private String emailId="ram@gmail.com";
	private String userName;
	private String password;
	private Boolean active;
	private UserType type;
	private Gender gender;
	private String tenantId; 
	
	@JsonProperty("roles")
    private List<Role> roles;
}
