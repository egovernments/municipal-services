package org.egov.wscalculation.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxHeadMaster {

	private String id;

	@NotNull
	private String tenantId;
	@Valid
	@NotNull
	private Category category;
	@NotNull
	private String service;
	@NotNull
	private String name;

	private String code;
	
	private List<GlCodeMaster> glCodes;

	private Boolean isDebit = false;

	private Boolean isActualDemand;
	@NotNull
	private Long validFrom;
	@NotNull
	private Long validTill;
	
	private Integer order;

	private AuditDetails auditDetail;
	

}
