package org.egov.pt.calculator.web.models;

        import java.util.List;

        import javax.validation.Valid;

        import org.egov.common.contract.request.RequestInfo;

        import com.fasterxml.jackson.annotation.JsonProperty;

        import lombok.AllArgsConstructor;
        import lombok.Builder;
        import lombok.Getter;
        import lombok.NoArgsConstructor;
        import lombok.Setter;

/**
 * MutationBillingSlabReq
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MutationBillingSlabReq   {
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("MutationBillingSlab")
    @Valid
    private List<MutationBillingSlab> mutationBillingSlab;


    public MutationBillingSlabReq addMutationBillingSlabItem(MutationBillingSlab mutationBillingSlabItem) {
        this.mutationBillingSlab.add(mutationBillingSlabItem);
        return this;
    }

}


