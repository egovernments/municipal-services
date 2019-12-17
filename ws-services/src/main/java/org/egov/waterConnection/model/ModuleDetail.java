package org.egov.waterConnection.model;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDetail {
	
	@NotNull
	private String moduleName;
	
	private List<MasterDetail> masterDetails;

}
