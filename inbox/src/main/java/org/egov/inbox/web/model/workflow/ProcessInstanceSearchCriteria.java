package org.egov.inbox.web.model.workflow;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProcessInstanceSearchCriteria {

    @JsonProperty("tenantId")
    private String tenantId;

    @JsonProperty("status")
    private List<String> status;

    @JsonProperty("businessIds")
    private List<String> businessIds;

    @JsonProperty("assignee")
    private String  assignee;

    @JsonProperty("ids")
    private List<String> ids;

    @JsonProperty("history")
    private Boolean history = false;

    @JsonProperty("fromDate")
    private Long fromDate = null;

    @JsonProperty("toDate")
    private Long toDate = null;


    @JsonProperty("offset")
    private Integer offset;

    @JsonProperty("limit")
    private Integer limit;

    @JsonProperty("businessService")
    private List<String> businessService;

    @JsonProperty("creationReason")
    private List<String> creationReason;

    @JsonProperty("moduleName")
    private String moduleName;


    public Boolean isNull(){
        if(this.getBusinessIds()==null && this.getIds()==null && this.getAssignee()==null &&
                this.getStatus()==null)
            return true;
        else return false;
    }



}
