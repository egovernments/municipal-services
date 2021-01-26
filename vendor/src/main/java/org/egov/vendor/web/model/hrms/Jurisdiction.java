package org.egov.vendor.web.model.hrms;


import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.egov.vendor.web.model.AuditDetails;
import org.springframework.validation.annotation.Validated;

@Validated
@EqualsAndHashCode(exclude = {"auditDetails"})
@Builder
@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
@ToString
public class Jurisdiction {

    private String id;

    @NotNull
    @Size(min=2, max=100)
    private String hierarchy;

    @NotNull
    @Size(min=2, max=100)
    private String boundary;

    @NotNull
    @Size(max=256)
    private String boundaryType;
    
    private String tenantId;

    private AuditDetails auditDetails;

    private Boolean isActive;

}
