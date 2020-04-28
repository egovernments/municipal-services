package org.egov.bpa.calculator.web.models.bpa;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.egov.common.contract.response.ResponseInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BPAResponse {

	 @JsonProperty("ResponseInfo")
     private ResponseInfo responseInfo = null;

     @JsonProperty("Licenses")
     @Valid
     private List<BPA> bpa = null;


     public BPAResponse addLicensesItem(BPA bpaItem) {
         if (this.bpa == null) {
         this.bpa = new ArrayList<>();
         }
     this.bpa.add(bpaItem);
     return this;
     }
}
