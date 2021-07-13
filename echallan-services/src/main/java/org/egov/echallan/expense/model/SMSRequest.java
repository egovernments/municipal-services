package org.egov.echallan.expense.model;

import lombok.*;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SMSRequest {
    private String mobileNumber;
    private String message;
    private String templateId;
    private String[] users; 

}
