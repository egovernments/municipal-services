package org.egov.pgr.contract;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class MigrationCriteria {


    private Set<String> tenantIds;

    private Integer offset;




}
