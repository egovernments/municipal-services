package org.egov.pt.models.hrms;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Locality {
    private String code;
    private String name;
    private String label;
    private Double latitude;
    private Double longitude;
    private String area;
    private List<Locality> children;
    private String materializedPath;

   
}
