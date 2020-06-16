package org.egov.waterconnection.model;


import lombok.*;
import org.springframework.validation.annotation.Validated;

@Validated
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@NoArgsConstructor
@Setter
@ToString
@Builder
public class DemoInput {
    public String input;
}
