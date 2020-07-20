package org.egov.pgr.web.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestSearchCriteria {

    @NotNull
    private String tenantId;

    private String serviceCode;

    private String mobileNo;

    private Set<String> ids;

}
