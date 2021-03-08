package org.egov.pgr.web.models;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoEscalationCriteria {

    private Set<String> tenantIds;

    private Integer offset;

    private Set<String> processInstanceIds;

}
